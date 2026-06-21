package com.bixis.bixismod.minigame;

import com.bixis.bixismod.config.BixisArenaSpawnsConfig;
import com.bixis.bixismod.config.BixisRaceSettingsConfig;
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
import net.minecraft.world.entity.Entity;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Hazırlık 2, Kapışma ve Sonuç fazlarını yönetir.
 * Arena ışınlama, PVP countdown (konum kilidi dahil), K/D takibi, sonuç sidebar'ı.
 * Bkz. MINIGAME_DESIGN.md Bölüm 3.4–3.7.
 */
@Mod.EventBusSubscriber(modid = "bixis")
public final class ArenaManager {

    public static final ArenaManager INSTANCE = new ArenaManager();
    private static final Logger LOGGER = LogManager.getLogger("Bixis");

    private static final String SIDEBAR_OBJ = "bixis_arena";
    private static final String[] TEAM_IDS   = {"bixis_team1","bixis_team2","bixis_team3","bixis_team4"};
    private static final String[] COLOR_CODES = {"§c","§a","§e","§9"};

    // ─── Countdown state ────────────────────────────────────────────────────
    /** -1 = pasif, 0+ = countdown tick sayısı */
    private int fightCountdownTick = -1;
    /** -1 = pasif (countdown devam ediyor veya başlamadı), 0+ = PVP tick sayısı */
    private int pvpTimerTick       = -1;
    private int sidebarTick        = 0;
    private boolean sidebarVisible = false;

    // ─── Position lock ───────────────────────────────────────────────────────
    private final Map<UUID, double[]>   frozenPositions = new HashMap<>();
    private final Map<UUID, ServerLevel> frozenLevels   = new HashMap<>();

    // ─── PVP istatistikleri ─────────────────────────────────────────────────
    private final Map<UUID, Integer> pvpKillCount  = new HashMap<>();
    private final Map<UUID, Integer> pvpDeathCount = new HashMap<>();

    // ─── Arena respawn bekleyenler ───────────────────────────────────────────
    private final Set<UUID> pendingArenaRespawn = new HashSet<>();

    private ArenaManager() {}

    // ────────────────────────────────────────────────────────────────────────
    //  Forge event hooks
    // ────────────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (INSTANCE.fightCountdownTick < 0 && INSTANCE.pvpTimerTick < 0
                && INSTANCE.pendingArenaRespawn.isEmpty()) return;
        INSTANCE.tick();
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (GameStateManager.INSTANCE.getState() != GameState.KAPISMA) return;
        if (INSTANCE.pvpTimerTick < 0) return; // countdown henüz bitmedi

        if (event.getEntity() instanceof ServerPlayer dead) {
            INSTANCE.pvpDeathCount.merge(dead.getUUID(), 1, Integer::sum);
            // Kill sadece oyuncu-oyuncu öldürmelerinde sayılır
            Entity killer = event.getSource().getEntity();
            if (killer instanceof ServerPlayer kp) {
                INSTANCE.pvpKillCount.merge(kp.getUUID(), 1, Integer::sum);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        GameState state = GameStateManager.INSTANCE.getState();
        if (state != GameState.HAZIRLIK_2 && state != GameState.KAPISMA) return;
        // Sonraki tick'te işle — respawn paketleri gönderilirken direkt ışınlama çakışabilir
        INSTANCE.pendingArenaRespawn.add(event.getEntity().getUUID());
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────────────────

    /** Tüm oyuncuları takımlarının arena spawn'ına ışınlar. */
    public void teleportToArena(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        int count = 0;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (teleportPlayerToArena(p, server, sb)) count++;
        }
        LOGGER.info("[Bixis] {} oyuncu arenaya ışınlandı.", count);
    }

    /**
     * Oyuncuları arenaya ışınlar ve 3-2-1 countdown başlatır.
     * Countdown bitiminde GameState → KAPISMA geçişi ve PVP timer başlar.
     * /bixis kapisma_basla tarafından çağrılır.
     */
    public void startFight(MinecraftServer server) {
        pvpKillCount.clear();
        pvpDeathCount.clear();
        frozenPositions.clear();
        frozenLevels.clear();
        pvpTimerTick = -1;
        sidebarTick  = 0;

        // Temiz başlangıç için arena ışınlaması
        teleportToArena(server);

        // Countdown başlıyor — tick 0'da pozisyonlar dondurulacak
        fightCountdownTick = 0;
    }

    /** Tüm state'i sıfırlar. /bixis sifirla tarafından çağrılır. */
    public void reset(MinecraftServer server) {
        fightCountdownTick = -1;
        pvpTimerTick       = -1;
        frozenPositions.clear();
        frozenLevels.clear();
        pvpKillCount.clear();
        pvpDeathCount.clear();
        pendingArenaRespawn.clear();
        hideSidebar(server);
    }

    private void processPendingRespawns(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        for (UUID id : pendingArenaRespawn) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) {
                teleportPlayerToArena(p, server, sb);
            }
        }
        pendingArenaRespawn.clear();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Dahili tick mantığı
    // ────────────────────────────────────────────────────────────────────────

    private void tick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        // Arena respawn bekleyenler (HAZIRLIK_2 veya KAPISMA ölümleri)
        if (!pendingArenaRespawn.isEmpty()) {
            processPendingRespawns(server);
        }

        // Countdown aşaması (HAZIRLIK_2 → KAPISMA)
        if (fightCountdownTick >= 0) {
            if (!frozenPositions.isEmpty()) freezeAllPlayers(server);
            tickCountdown(server);
            if (fightCountdownTick >= 0) fightCountdownTick++;
        }

        // PVP timer aşaması (KAPISMA)
        if (pvpTimerTick >= 0) {
            pvpTimerTick++;
            if (++sidebarTick >= 20) {
                sidebarTick = 0;
                updateKapismaSidebar(server);
            }
            if (pvpTimerTick >= BixisRaceSettingsConfig.getPvpTimeSecs() * 20) {
                endFight(server);
            }
        }
    }

    private void tickCountdown(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        if (fightCountdownTick == 0) {
            // Pozisyonları şimdi dondur (arena ışınlaması zaten gerçekleşti)
            for (ServerPlayer p : players) {
                frozenPositions.put(p.getUUID(), new double[]{p.getX(), p.getY(), p.getZ()});
                frozenLevels.put(p.getUUID(), (ServerLevel) p.level());
            }
            sendTitleAll(players, "3", ChatFormatting.YELLOW);
            playTickSound(players, 0.8f);
        } else if (fightCountdownTick == 20) {
            sendTitleAll(players, "2", ChatFormatting.YELLOW);
            playTickSound(players, 1.0f);
        } else if (fightCountdownTick == 40) {
            sendTitleAll(players, "1", ChatFormatting.RED);
            playTickSound(players, 1.2f);
        } else if (fightCountdownTick == 60) {
            frozenPositions.clear();
            frozenLevels.clear();

            Component title    = Component.literal("BAŞLA!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
            Component subtitle = Component.literal(BixisRaceSettingsConfig.getPvpTimeMins() + " dakika!").withStyle(ChatFormatting.YELLOW);
            for (ServerPlayer p : players) {
                p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 20));
                p.connection.send(new ClientboundSetTitleTextPacket(title));
                p.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                p.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.MASTER, 1.0f, 1.0f);
            }

            pvpTimerTick = 0;
            GameStateManager.INSTANCE.setState(GameState.KAPISMA);
            fightCountdownTick = -1;
        }
    }

    /** Dondurulmuş konumdan sapan oyuncuları geri ışınlar; bakış açısı korunur. */
    private void freezeAllPlayers(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            double[] pos    = frozenPositions.get(p.getUUID());
            ServerLevel lvl = frozenLevels.get(p.getUUID());
            if (pos == null || lvl == null) continue;
            double dx = p.getX() - pos[0];
            double dy = p.getY() - pos[1];
            double dz = p.getZ() - pos[2];
            if (dx * dx + dy * dy + dz * dz > 0.0001) {
                p.teleportTo(lvl, pos[0], pos[1], pos[2], p.getYRot(), p.getXRot());
            }
        }
    }

    private void endFight(MinecraftServer server) {
        pvpTimerTick = -1;

        Component title = Component.literal("KAPIŞMA BİTTİ!")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
            p.connection.send(new ClientboundSetTitleTextPacket(title));
            p.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.MASTER, 1.0f, 1.0f);
        }

        GameStateManager.INSTANCE.setState(GameState.SONUC);
        RaceManager.INSTANCE.printRaceResults(server);
        printFightStats(server);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Sidebar
    // ────────────────────────────────────────────────────────────────────────

    private void updateKapismaSidebar(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        Objective old = sb.getObjective(SIDEBAR_OBJ);
        if (old != null) sb.removeObjective(old);

        Objective obj = sb.addObjective(
            SIDEBAR_OBJ, ObjectiveCriteria.DUMMY,
            Component.literal("KAPIŞMA").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
            ObjectiveCriteria.RenderType.INTEGER
        );
        sb.setDisplayObjective(1, obj);

        int pvpTimeSecs = BixisRaceSettingsConfig.getPvpTimeSecs();
        int remaining   = Math.max(0, pvpTimeSecs - pvpTimerTick / 20);
        String timeStr  = String.format("%02d:%02d", remaining / 60, remaining % 60);

        List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
        players.sort((a, b) -> pvpKillCount.getOrDefault(b.getUUID(), 0)
                              - pvpKillCount.getOrDefault(a.getUUID(), 0));

        int score = players.size() + 2; // timer + separator + oyuncular
        setLine(sb, obj, "§f" + timeStr, score--);
        setLine(sb, obj, "§8─────────", score--);

        for (ServerPlayer p : players) {
            int k = pvpKillCount.getOrDefault(p.getUUID(), 0);
            int d = pvpDeathCount.getOrDefault(p.getUUID(), 0);
            setLine(sb, obj, teamColor(p, server) + p.getName().getString() + ": " + k + "K / " + d + "D", score--);
        }

        sidebarVisible = true;
    }

    private void hideSidebar(MinecraftServer server) {
        if (!sidebarVisible) return;
        Scoreboard sb  = server.getScoreboard();
        Objective obj  = sb.getObjective(SIDEBAR_OBJ);
        if (obj != null) sb.removeObjective(obj);
        sidebarVisible = false;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Yardımcılar
    // ────────────────────────────────────────────────────────────────────────

    private boolean teleportPlayerToArena(ServerPlayer player, MinecraftServer server, Scoreboard sb) {
        PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
        if (team == null) return false;
        int idx = teamIndex(team.getName());
        if (idx < 0) return false;
        int teamNum = idx + 1;

        Optional<SpawnPoint> spOpt = BixisArenaSpawnsConfig.getSpawn(teamNum);
        if (spOpt.isEmpty()) {
            LOGGER.warn("[Bixis] Takım {} arena spawn ayarlanmamış, {} ışınlanmıyor.", teamNum, player.getName().getString());
            return false;
        }
        SpawnPoint sp = spOpt.get();
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(sp.dimension()));
        ServerLevel level = server.getLevel(dimKey);
        if (level == null) return false;
        player.teleportTo(level, sp.x(), sp.y(), sp.z(), sp.yaw(), 0f);
        return true;
    }

    /** Kapışma sonu K/D istatistiklerini chat'e döker. */
    private void printFightStats(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        Component header = Component.literal("─── Kapışma Sonuçları ───").withStyle(ChatFormatting.GOLD);
        for (ServerPlayer p : players) p.sendSystemMessage(header);

        List<ServerPlayer> sorted = new ArrayList<>(players);
        sorted.sort((a, b) -> pvpKillCount.getOrDefault(b.getUUID(), 0)
                             - pvpKillCount.getOrDefault(a.getUUID(), 0));
        for (ServerPlayer p : sorted) {
            int k = pvpKillCount.getOrDefault(p.getUUID(), 0);
            int d = pvpDeathCount.getOrDefault(p.getUUID(), 0);
            Component line = Component.literal(p.getName().getString() + ": " + k + " Öldürme / " + d + " Ölüm")
                .withStyle(ChatFormatting.YELLOW);
            for (ServerPlayer q : players) q.sendSystemMessage(line);
        }
    }

    private String teamColor(ServerPlayer player, MinecraftServer server) {
        Scoreboard sb   = server.getScoreboard();
        PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
        if (team == null) return "§f";
        int idx = teamIndex(team.getName());
        return idx >= 0 ? COLOR_CODES[idx] : "§f";
    }

    private static void sendTitleAll(List<ServerPlayer> players, String text, ChatFormatting color) {
        Component title = Component.literal(text).withStyle(color, ChatFormatting.BOLD);
        for (ServerPlayer p : players) {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 10, 5));
            p.connection.send(new ClientboundSetTitleTextPacket(title));
        }
    }

    private static void playTickSound(List<ServerPlayer> players, float pitch) {
        for (ServerPlayer p : players) {
            p.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f, pitch);
        }
    }

    private static void setLine(Scoreboard sb, Objective obj, String text, int score) {
        Score s = sb.getOrCreatePlayerScore(text, obj);
        s.setScore(score);
    }

    private static int teamIndex(String teamId) {
        for (int i = 0; i < TEAM_IDS.length; i++) {
            if (TEAM_IDS[i].equals(teamId)) return i;
        }
        return -1;
    }
}
