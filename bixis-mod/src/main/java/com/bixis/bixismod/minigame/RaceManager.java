package com.bixis.bixismod.minigame;

import com.bixis.bixismod.config.BixisCheckpointsConfig;
import com.bixis.bixismod.config.BixisRaceSettingsConfig;
import com.bixis.bixismod.config.BixisRaceSpawnsConfig;
import com.bixis.bixismod.config.SpawnPoint;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Yarış fazı yöneticisi.
 * Checkpoint takibi, ölüm sayacı, bitiş kaydı, sidebar ve otomatik bitiş.
 * Bkz. MINIGAME_DESIGN.md Bölüm 3.3.
 */
@Mod.EventBusSubscriber(modid = "bixis")
public final class RaceManager {

    public static final RaceManager INSTANCE = new RaceManager();
    private static final Logger LOGGER = LogManager.getLogger("Bixis");

    private static final String SIDEBAR_OBJ = "bixis_race";
    private static final String[] TEAM_IDS   = {"bixis_team1","bixis_team2","bixis_team3","bixis_team4"};
    private static final String[] COLOR_CODES = {"§c","§a","§e","§9"};

    // ─── Per-player state ───────────────────────────────────────────────────
    /** 0-based index son geçilen checkpoint (-1 = hiç geçilmedi) */
    private final Map<UUID, Integer> lastCheckpoint  = new HashMap<>();
    /** Yarış fazındaki toplam ölüm sayısı */
    private final Map<UUID, Integer> deathCount      = new HashMap<>();
    /** Bitiriş süresi (ms); ancak bitirmişlerde var */
    private final Map<UUID, Long>    finishTimeMs    = new HashMap<>();
    /** Bitiriş sırasıyla UUID listesi */
    private final List<UUID>         finishOrder     = new ArrayList<>();
    /** Takım bazlı bitiriş sayaçları (0-indexed) */
    private final int[]              teamFinishCount  = new int[4];
    /** Respawn eventinden sonraki tick'te ışınlanacak oyuncular */
    private final Set<UUID>          pendingRespawn  = new HashSet<>();
    /** SONUC sidebar'ı için oyuncu adları (UUID → görünen ad) */
    private final Map<UUID, String>  racePlayerNames = new HashMap<>();

    // ─── Race state ─────────────────────────────────────────────────────────
    private long raceStartMs    = 0;
    /** -1 = pasif, >=0 = geçen tick sayısı */
    private int  raceTick       = -1;
    private int  checkpointScan = 0;
    private int  sidebarTick    = 0;
    private boolean sidebarVisible = false;

    private RaceManager() {}

    // ────────────────────────────────────────────────────────────────────────
    //  Forge event hooks
    // ────────────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (GameStateManager.INSTANCE.getState() != GameState.YARIS) return;
        INSTANCE.tick();
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (GameStateManager.INSTANCE.getState() != GameState.YARIS) return;
        INSTANCE.deathCount.merge(player.getUUID(), 1, Integer::sum);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (GameStateManager.INSTANCE.getState() != GameState.YARIS) return;
        if (event.isEndConquered()) return; // portal geçişi, ölüm değil
        INSTANCE.pendingRespawn.add(event.getEntity().getUUID());
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────────────────

    /** Yarışı başlatır. LobbyManager'ın geri sayımı bitince çağrılır. */
    public void start(MinecraftServer server) {
        lastCheckpoint.clear();
        deathCount.clear();
        finishTimeMs.clear();
        finishOrder.clear();
        pendingRespawn.clear();
        racePlayerNames.clear();
        java.util.Arrays.fill(teamFinishCount, 0);

        raceStartMs = System.currentTimeMillis();
        raceTick = 0;
        checkpointScan = 0;
        sidebarTick = 0;

        // Tüm oyuncuları başlat
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            lastCheckpoint.put(p.getUUID(), -1);
            deathCount.put(p.getUUID(), 0);
            racePlayerNames.put(p.getUUID(), p.getName().getString());
        }

        LOGGER.info("[Bixis] Yarış başladı ({} dk).", BixisRaceSettingsConfig.getRaceTimeMins());
    }

    /**
     * Yarışı durdurur. State YARIS dışına çıkınca (sifirla, bitiş) çağrılır.
     * Sidebar'ı kaldırır.
     */
    public void stop(MinecraftServer server) {
        if (raceTick < 0) return; // zaten pasif
        raceTick = -1;
        hideSidebar(server);
    }

    /**
     * Oyuncunun yarışı bitirdiğini kaydeder.
     *
     * @return false oyuncu zaten bitirmişse (komut yoksayılmalı)
     */
    public boolean finishPlayer(ServerPlayer player) {
        UUID id = player.getUUID();
        if (finishTimeMs.containsKey(id)) return false;

        long elapsed = System.currentTimeMillis() - raceStartMs;
        finishTimeMs.put(id, elapsed);
        finishOrder.add(id);
        racePlayerNames.put(id, player.getName().getString());
        int place = finishOrder.size();

        // Takım bitiriş sayacını artır
        MinecraftServer server = player.getServer();
        if (server != null) {
            Scoreboard sb = server.getScoreboard();
            PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
            if (team != null) {
                int tidx = teamIndex(team.getName());
                if (tidx >= 0) teamFinishCount[tidx]++;
            }
        }

        String timeStr = formatTime(elapsed);

        // Oyuncuya title + subtitle
        Component title    = Component.literal("Bitirdin!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        Component subtitle = Component.literal(place + ". sıradasın").withStyle(ChatFormatting.YELLOW);
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0f, 1.0f);

        // Sunucuya broadcast
        if (server != null) {
            Component broadcast = Component.literal(
                player.getName().getString() + " yarışı bitirdi! (" + timeStr + ")")
                .withStyle(ChatFormatting.GOLD);
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(broadcast);
            }
        }

        return true;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Dahili tick mantığı
    // ────────────────────────────────────────────────────────────────────────

    private void tick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || raceTick < 0) return;

        raceTick++;

        // Ertelenmiş respawn ışınlamaları
        if (!pendingRespawn.isEmpty()) {
            processPendingRespawns(server);
        }

        // Checkpoint taraması (5 tick'te bir)
        if (++checkpointScan >= 5) {
            checkpointScan = 0;
            scanCheckpoints(server);
        }

        // Sidebar güncellemesi (saniyede bir = 20 tick)
        if (++sidebarTick >= 20) {
            sidebarTick = 0;
            updateSidebar(server);
        }

        // Erken bitiş: tüm online bixis-takım üyeleri bitirdiyse
        if (allTeamsFinished(server)) {
            endRace(server);
            return;
        }

        // Süre bitti mi?
        int maxTicks = BixisRaceSettingsConfig.getRaceTimeSecs() * 20;
        if (raceTick >= maxTicks) {
            endRace(server);
        }
    }

    private void processPendingRespawns(MinecraftServer server) {
        for (UUID id : new ArrayList<>(pendingRespawn)) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                respawnAtCheckpoint(player, server);
            }
        }
        pendingRespawn.clear();
    }

    private void respawnAtCheckpoint(ServerPlayer player, MinecraftServer server) {
        UUID id = player.getUUID();
        int cpIdx = lastCheckpoint.getOrDefault(id, -1);

        Scoreboard sb = server.getScoreboard();
        PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
        if (team == null) return;
        int teamNum = teamIndex(team.getName()) + 1;
        if (teamNum <= 0) return;

        SpawnPoint sp;
        if (cpIdx >= 0) {
            List<SpawnPoint> cps = BixisCheckpointsConfig.getCheckpoints(teamNum);
            if (cpIdx < cps.size()) {
                sp = cps.get(cpIdx);
            } else {
                sp = BixisRaceSpawnsConfig.getSpawn(teamNum).orElse(null);
            }
        } else {
            sp = BixisRaceSpawnsConfig.getSpawn(teamNum).orElse(null);
        }

        if (sp == null) {
            LOGGER.warn("[Bixis] {} için respawn noktası bulunamadı.", player.getName().getString());
            return;
        }

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(sp.dimension()));
        ServerLevel level = server.getLevel(dimKey);
        if (level == null) return;

        player.teleportTo(level, sp.x(), sp.y(), sp.z(), sp.yaw(), 0f);

        String msg = cpIdx >= 0
            ? "Checkpoint #" + (cpIdx + 1) + "'den devam ediyorsun!"
            : "Başlangıç noktasından devam ediyorsun!";
        player.sendSystemMessage(Component.literal(msg).withStyle(ChatFormatting.YELLOW));
    }

    private void scanCheckpoints(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            if (finishTimeMs.containsKey(id)) continue; // zaten bitirdi

            PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
            if (team == null) continue;
            int teamNum = teamIndex(team.getName()) + 1;
            if (teamNum <= 0) continue;

            List<SpawnPoint> cps = BixisCheckpointsConfig.getCheckpoints(teamNum);
            if (cps.isEmpty()) continue;

            int lastCp = lastCheckpoint.getOrDefault(id, -1);

            for (int i = lastCp + 1; i < cps.size(); i++) {
                SpawnPoint cp = cps.get(i);
                // Farklı dimension'da ise atla
                if (!cp.dimension().equals(player.level().dimension().location().toString())) continue;

                double dx = player.getX() - cp.x();
                double dy = player.getY() - cp.y();
                double dz = player.getZ() - cp.z();
                if (dx * dx + dy * dy + dz * dz <= 9.0) { // 3 blok yarıçap
                    lastCheckpoint.put(id, i);
                    player.sendSystemMessage(Component.literal(
                        "Checkpoint #" + (i + 1) + " geçildi!").withStyle(ChatFormatting.GREEN));
                    break; // aynı tick'te en fazla bir checkpoint ilerle
                }
            }
        }
    }

    private void endRace(MinecraftServer server) {
        raceTick = -1; // ticker'ı durdur

        // Bitirmeyenler için DNF kaydı
        long elapsed = System.currentTimeMillis() - raceStartMs;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            if (!finishTimeMs.containsKey(id)) {
                finishTimeMs.put(id, elapsed); // DNF — kesin bitiş yok
                racePlayerNames.put(id, player.getName().getString());
                LOGGER.info("[Bixis] DNF: {} (ölüm: {}, son CP: {})",
                    player.getName().getString(),
                    deathCount.getOrDefault(id, 0),
                    lastCheckpoint.getOrDefault(id, -1));
            }
        }

        // Title + ses
        Component title = Component.literal("Süre doldu!")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            p.connection.send(new ClientboundSetTitleTextPacket(title));
            p.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.MASTER, 1.0f, 1.0f);
        }

        hideSidebar(server);
        printRaceResults(server);

        // Sonraki faza geç
        GameStateManager.INSTANCE.setState(GameState.HAZIRLIK_1);
    }

    /** Yarış sonuçlarını chat'e döker. endRace ve ArenaManager.endFight tarafından çağrılır. */
    public void printRaceResults(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        Component header = Component.literal("─── Yarış Sonuçları ───").withStyle(ChatFormatting.GOLD);
        for (ServerPlayer p : players) p.sendSystemMessage(header);

        for (int i = 0; i < finishOrder.size(); i++) {
            UUID id      = finishOrder.get(i);
            String name  = racePlayerNames.getOrDefault(id, "???");
            String time  = formatTime(finishTimeMs.getOrDefault(id, 0L));
            int d        = deathCount.getOrDefault(id, 0);
            Component line = Component.literal((i + 1) + ". " + name + " - " + time + " - " + d + " Ölüm")
                .withStyle(ChatFormatting.WHITE);
            for (ServerPlayer p : players) p.sendSystemMessage(line);
        }

        // DNF oyuncuları (finishOrder'da olmayan ama kayıtlı isimler)
        for (Map.Entry<UUID, String> entry : racePlayerNames.entrySet()) {
            if (finishOrder.contains(entry.getKey())) continue;
            int d = deathCount.getOrDefault(entry.getKey(), 0);
            Component line = Component.literal(entry.getValue() + " - Bitirmedi - " + d + " Ölüm")
                .withStyle(ChatFormatting.GRAY);
            for (ServerPlayer p : players) p.sendSystemMessage(line);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Sidebar
    // ────────────────────────────────────────────────────────────────────────

    private void updateSidebar(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();

        Objective old = sb.getObjective(SIDEBAR_OBJ);
        if (old != null) sb.removeObjective(old);

        Objective obj = sb.addObjective(
            SIDEBAR_OBJ,
            ObjectiveCriteria.DUMMY,
            Component.literal("YARIŞ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
            ObjectiveCriteria.RenderType.INTEGER
        );
        sb.setDisplayObjective(1, obj);

        // Kalan süre
        int raceTimeSecs = BixisRaceSettingsConfig.getRaceTimeSecs();
        int elapsedSecs  = raceTick / 20;
        int remaining    = Math.max(0, raceTimeSecs - elapsedSecs);
        String timeStr   = String.format("%02d:%02d", remaining / 60, remaining % 60);

        // Satır 6 (en üst): kalan süre
        setLine(sb, obj, "§f" + timeStr, 6);

        // Satır 5: ayraç
        setLine(sb, obj, "§8─────────", 5);

        // Satır 4-1: takım bitiriş sayıları
        for (int i = 0; i < 4; i++) {
            setLine(sb, obj,
                COLOR_CODES[i] + "Takım " + (i + 1) + ": " + teamFinishCount[i] + " bitirdi",
                4 - i);
        }

        sidebarVisible = true;
    }

    private static void setLine(Scoreboard sb, Objective obj, String text, int score) {
        Score s = sb.getOrCreatePlayerScore(text, obj);
        s.setScore(score);
    }

    private void hideSidebar(MinecraftServer server) {
        if (!sidebarVisible) return;
        Scoreboard sb = server.getScoreboard();
        Objective obj = sb.getObjective(SIDEBAR_OBJ);
        if (obj != null) sb.removeObjective(obj);
        sidebarVisible = false;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Yardımcılar
    // ────────────────────────────────────────────────────────────────────────

    /** Milisaniyeyi "mm:ss.cs" formatına çevirir. */
    public static String formatTime(long ms) {
        long centis = (ms % 1000) / 10;
        long secs   = (ms / 1000) % 60;
        long mins   = ms / 60000;
        return String.format("%02d:%02d.%02d", mins, secs, centis);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  SONUC fazı için public getter'lar
    // ────────────────────────────────────────────────────────────────────────

    /** Bitiriş sırasıyla UUID listesi (sadece gerçekten bitirenler). */
    public List<UUID> getFinishOrder() {
        return Collections.unmodifiableList(finishOrder);
    }

    /** Tüm katılımcıların bitiriş süreleri (DNF dahil). */
    public Map<UUID, Long> getFinishTimeMs() {
        return Collections.unmodifiableMap(finishTimeMs);
    }

    /** Yarış fazındaki ölüm sayıları. */
    public Map<UUID, Integer> getRaceDeathCount() {
        return Collections.unmodifiableMap(deathCount);
    }

    /** Oyuncu adları (UUID → görünen ad, yarış sırasında kaydedilir). */
    public Map<UUID, String> getRacePlayerNames() {
        return Collections.unmodifiableMap(racePlayerNames);
    }

    /**
     * Tüm online bixis-takım üyeleri yarışı bitirdi mi?
     * Yarış başlamadan (raceTick=0) true dönmemesi için en az 1 bitiriş gerekir.
     */
    private boolean allTeamsFinished(MinecraftServer server) {
        if (finishOrder.isEmpty()) return false; // henüz kimse bitirmedi
        Scoreboard sb = server.getScoreboard();
        boolean anyPlayer = false;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
            if (team == null || teamIndex(team.getName()) < 0) continue;
            anyPlayer = true;
            if (!finishTimeMs.containsKey(player.getUUID())) return false;
        }
        return anyPlayer;
    }

    private static int teamIndex(String teamId) {
        for (int i = 0; i < TEAM_IDS.length; i++) {
            if (TEAM_IDS[i].equals(teamId)) return i;
        }
        return -1;
    }
}
