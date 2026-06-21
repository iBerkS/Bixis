package com.bixis.bixismod.minigame;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.server.ServerStartedEvent;

import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

/**
 * Lobi fazı yöneticisi.
 * Scoreboard takımları, hazır flag'leri ve sidebar scoreboard.
 * Bkz. MINIGAME_DESIGN.md Bölüm 3.1 ve Bölüm 6.
 */
@Mod.EventBusSubscriber(modid = "bixis")
public final class LobbyManager {

    public static final LobbyManager INSTANCE = new LobbyManager();

    private static final Logger LOGGER = LogManager.getLogger("Bixis");

    private static final String[] TEAM_IDS   = {"bixis_team1", "bixis_team2", "bixis_team3", "bixis_team4"};
    private static final ChatFormatting[] TEAM_COLORS = {
        ChatFormatting.RED, ChatFormatting.GREEN, ChatFormatting.YELLOW, ChatFormatting.BLUE
    };
    // Sidebar'da satır başı renk kodu — ANSI-benzeri Minecraft §
    private static final String[] COLOR_CODES = {"§c", "§a", "§e", "§9"};

    private static final String SIDEBAR_OBJ = "bixis_lobby";

    private final boolean[] readyFlags = new boolean[4];
    private int tickCounter = 0;
    private boolean sidebarVisible = false;

    // Geri sayım: -1 = pasif, 0+ = aktif (tick sayacı)
    private int countdownTick = -1;

    private LobbyManager() {}

    // ────────────────────────────────────────────────────────
    //  Forge event hooks (static dispatch → INSTANCE)
    // ────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        INSTANCE.reset(event.getServer());
    }

    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        INSTANCE.tick();
    }

    // ────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────

    /**
     * Tüm takımları ve hazır flag'lerini sıfırlar.
     * /bixis sifirla ve sunucu başlangıcında çağrılır.
     */
    public void reset(MinecraftServer server) {
        java.util.Arrays.fill(readyFlags, false);
        RaceManager.INSTANCE.stop(server); // yarış sırasında sıfırlanırsa race'i durdur
        ArenaManager.INSTANCE.reset(server); // arena/kapışma/sonuç sırasında sıfırlanırsa temizle
        countdownTick = -1;
        setupTeams(server);
        updateSidebar(server);
    }

    /**
     * Tüm dolu takımlar hazır mı? /bixis basla kontrolü için kullanılır.
     *
     * @return true yalnızca en az bir dolu takım varsa VE hepsi hazırsa
     */
    public boolean allReadyCheck(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        boolean anyTeam = false;
        for (int i = 0; i < 4; i++) {
            PlayerTeam team = sb.getPlayerTeam(TEAM_IDS[i]);
            int count = (team != null) ? team.getPlayers().size() : 0;
            if (count > 0) {
                anyTeam = true;
                if (!readyFlags[i]) return false;
            }
        }
        return anyTeam;
    }

    /**
     * Geri sayım sekansını başlatır. GameState GERI_SAYIM olarak
     * ayarlandıktan sonra çağrılmalı.
     */
    public void startCountdown(MinecraftServer server) {
        countdownTick = 0;
    }

    /**
     * Oyuncuyu belirtilen takıma ekler (1-4 arası).
     * Eski takımından ve yeni takımın hazır flag'inden çıkar.
     */
    public void joinTeam(ServerPlayer player, int teamNum) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        Scoreboard sb = server.getScoreboard();
        String playerName = player.getScoreboardName();

        // Aynı takım tekrar seçildiyse işlem yapma
        PlayerTeam current = sb.getPlayersTeam(playerName);
        if (current != null && TEAM_IDS[teamNum - 1].equals(current.getName())) {
            player.sendSystemMessage(
                Component.literal("⚠ Zaten " + teamNum + ". takımdasın!")
                    .withStyle(ChatFormatting.DARK_RED)
            );
            playError(player);
            return;
        }

        // Eski takımdan çıkar, o takımın hazır flag'ini sıfırla
        if (current != null) {
            int oldIdx = teamIndex(current.getName());
            if (oldIdx >= 0) readyFlags[oldIdx] = false;
            sb.removePlayerFromTeam(playerName, current);
        }

        // Yeni takımın hazır flag'ini de sıfırla (üye değişti)
        readyFlags[teamNum - 1] = false;

        PlayerTeam newTeam = sb.getPlayerTeam(TEAM_IDS[teamNum - 1]);
        if (newTeam != null) {
            sb.addPlayerToTeam(playerName, newTeam);
        }

        player.sendSystemMessage(
            Component.literal(teamNum + ". takıma girdin!")
                .withStyle(TEAM_COLORS[teamNum - 1])
        );
        playSuccess(player);
    }

    /**
     * Oyuncunun takımını hazır olarak işaretler.
     * Oyuncu belirtilen takımda değilse hata mesajı gönderir.
     */
    public void setReady(ServerPlayer player, int teamNum) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        Scoreboard sb = server.getScoreboard();

        PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
        if (team == null || !TEAM_IDS[teamNum - 1].equals(team.getName())) {
            player.sendSystemMessage(
                Component.literal("⚠ Sen bu takımda değilsin!")
                    .withStyle(ChatFormatting.DARK_RED)
            );
            playError(player);
            return;
        }

        readyFlags[teamNum - 1] = true;

        Component broadcast = Component.literal("Takım " + teamNum + " Hazır!")
            .withStyle(TEAM_COLORS[teamNum - 1]);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(broadcast);
        }

        playSuccess(player);
        checkAllReady(server);
    }

    // ────────────────────────────────────────────────────────
    //  Dahili
    // ────────────────────────────────────────────────────────

    private void tick() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        // Geri sayım sekansı (tick-hassas)
        if (countdownTick >= 0) {
            tickCountdown(server);
            if (countdownTick >= 0) countdownTick++; // -1 atandıysa (sekans bitti) artırma
        }

        // Sidebar güncellemesi (her 20 tick)
        if (++tickCounter < 20) return;
        tickCounter = 0;

        if (GameStateManager.INSTANCE.getState() == GameState.LOBI) {
            updateSidebar(server);
        } else if (sidebarVisible) {
            hideSidebar(server);
        }
    }

    private void tickCountdown(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        if (countdownTick == 0) {
            sendTitleAll(players, "3", ChatFormatting.YELLOW);
            playTickSound(players, 0.8f);
        } else if (countdownTick == 20) {
            sendTitleAll(players, "2", ChatFormatting.YELLOW);
            playTickSound(players, 1.0f);
        } else if (countdownTick == 40) {
            sendTitleAll(players, "1", ChatFormatting.RED);
            playTickSound(players, 1.2f);
        } else if (countdownTick == 60) {
            teleportAndKit(server, players);
            sendStartTitle(players);
            for (ServerPlayer p : players) {
                p.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.MASTER, 1.0f, 1.0f);
            }
            GameStateManager.INSTANCE.setState(GameState.YARIS);
            RaceManager.INSTANCE.start(server);
            countdownTick = -1; // sekans bitti, bir daha artırılmasın
        }
    }

    private void teleportAndKit(MinecraftServer server, List<ServerPlayer> players) {
        Scoreboard sb = server.getScoreboard();
        for (ServerPlayer player : players) {
            PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
            if (team == null) continue;
            int idx = teamIndex(team.getName());
            if (idx < 0) continue;
            int teamNum = idx + 1;

            var spawnOpt = com.bixis.bixismod.config.BixisRaceSpawnsConfig.getSpawn(teamNum);
            if (spawnOpt.isEmpty()) {
                LOGGER.warn("[Bixis] Takım {} için race spawn ayarlanmamış, {} ışınlanmıyor.",
                    teamNum, player.getName().getString());
                continue;
            }
            var sp = spawnOpt.get();
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION,
                new ResourceLocation(sp.dimension()));
            ServerLevel level = server.getLevel(dimKey);
            if (level == null) {
                LOGGER.warn("[Bixis] Dimension '{}' bulunamadı, takım {} ışınlanmıyor.",
                    sp.dimension(), teamNum);
                continue;
            }
            player.teleportTo(level, sp.x(), sp.y(), sp.z(), sp.yaw(), 0f);
            player.setGameMode(GameType.SURVIVAL);
            giveKit(player);
        }
    }

    private static void giveKit(ServerPlayer player) {
        player.getInventory().add(new ItemStack(Items.IRON_PICKAXE, 1));
        player.getInventory().add(new ItemStack(Items.COOKED_BEEF, 10));
        player.getInventory().add(new ItemStack(Items.COBBLESTONE, 32));
    }

    private static void sendTitleAll(List<ServerPlayer> players, String text, ChatFormatting color) {
        Component title = Component.literal(text).withStyle(color, ChatFormatting.BOLD);
        for (ServerPlayer p : players) {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 10, 5));
            p.connection.send(new ClientboundSetTitleTextPacket(title));
        }
    }

    private static void sendStartTitle(List<ServerPlayer> players) {
        Component title    = Component.literal("YARIŞ BAŞLADI!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
        Component subtitle = Component.literal("Bol şans!").withStyle(ChatFormatting.YELLOW);
        for (ServerPlayer p : players) {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 20));
            p.connection.send(new ClientboundSetTitleTextPacket(title));
            p.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }

    private void setupTeams(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        for (int i = 0; i < 4; i++) {
            String id = TEAM_IDS[i];
            PlayerTeam existing = sb.getPlayerTeam(id);
            if (existing != null) {
                // Tüm üyeleri çıkar
                final PlayerTeam toClean = existing;
                new HashSet<>(toClean.getPlayers())
                    .forEach(p -> sb.removePlayerFromTeam(p, toClean));
            } else {
                existing = sb.addPlayerTeam(id);
            }
            PlayerTeam team = existing;
            team.setColor(TEAM_COLORS[i]);
            team.setDisplayName(Component.literal("Takım " + (i + 1)));
        }
    }

    private void updateSidebar(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();

        // Eskiyi kaldır, yeniden oluştur (satır metni değişebilir)
        Objective old = sb.getObjective(SIDEBAR_OBJ);
        if (old != null) sb.removeObjective(old);

        Objective obj = sb.addObjective(
            SIDEBAR_OBJ,
            ObjectiveCriteria.DUMMY,
            Component.literal("LOBİ DURUMU").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            ObjectiveCriteria.RenderType.INTEGER
        );
        sb.setDisplayObjective(1, obj); // 1 = sidebar slot

        for (int i = 0; i < 4; i++) {
            PlayerTeam team = sb.getPlayerTeam(TEAM_IDS[i]);
            int count = (team != null) ? team.getPlayers().size() : 0;

            String status;
            if (count == 0)          status = "-";
            else if (readyFlags[i])  status = "✔";
            else                     status = "✘";

            // Fake "oyuncu adı" = görüntülenecek satır metni
            String line = COLOR_CODES[i] + "Takım " + (i + 1) + ": " + count + " oyuncu - " + status;
            Score score = sb.getOrCreatePlayerScore(line, obj);
            score.setScore(4 - i); // Takım 1 en üstte (score=4), Takım 4 en altta (score=1)
        }

        sidebarVisible = true;
    }

    private void hideSidebar(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        Objective obj = sb.getObjective(SIDEBAR_OBJ);
        if (obj != null) sb.removeObjective(obj);
        sidebarVisible = false;
    }

    private void checkAllReady(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        boolean anyTeam = false;
        boolean allReady = true;

        for (int i = 0; i < 4; i++) {
            PlayerTeam team = sb.getPlayerTeam(TEAM_IDS[i]);
            int count = (team != null) ? team.getPlayers().size() : 0;
            if (count > 0) {
                anyTeam = true;
                if (!readyFlags[i]) {
                    allReady = false;
                    break;
                }
            }
        }

        if (anyTeam && allReady) {
            LOGGER.info("[Bixis] Tüm takımlar hazır, /bixis basla kullanılabilir");
        }
    }

    /** Geri sayım tik sesi — 3/2/1 title'larında; pitch artar (0.8→1.0→1.2). */
    private static void playTickSound(List<ServerPlayer> players, float pitch) {
        for (ServerPlayer p : players) {
            p.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f, pitch);
        }
    }

    /** Başarı sesi — kısa UI click. */
    public static void playSuccess(ServerPlayer player) {
        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 1.0f, 1.0f);
    }

    /** Hata sesi — köylü "hayır" sesi. */
    public static void playError(ServerPlayer player) {
        player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.MASTER, 1.0f, 1.0f);
    }

    /** "bixis_teamN" → N-1 (0-indexed), eşleşme yoksa -1 */
    private static int teamIndex(String teamId) {
        for (int i = 0; i < TEAM_IDS.length; i++) {
            if (TEAM_IDS[i].equals(teamId)) return i;
        }
        return -1;
    }
}
