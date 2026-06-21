package com.bixis.bixismod.command;

import com.bixis.bixismod.config.BixisArenaSpawnsConfig;
import com.bixis.bixismod.config.BixisRaceSettingsConfig;
import com.bixis.bixismod.minigame.ArenaManager;
import com.bixis.bixismod.config.BixisCheckpointsConfig;
import com.bixis.bixismod.config.BixisRaceSpawnsConfig;
import com.bixis.bixismod.config.SpawnPoint;
import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.minigame.GameState;
import com.bixis.bixismod.minigame.RaceManager;
import com.bixis.bixismod.minigame.GameStateManager;
import com.bixis.bixismod.minigame.LobbyManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** /bixis komutlarını kaydeden sınıf. */
public final class BixisCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Tüm /bixis alt komutlarını dispatcher'a kaydeder.
     *
     * @param dispatcher sunucu komut dispatcher'ı
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("bixis");

        root.then(
            Commands.literal("ping")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("Bixis Mod aktif!"), false);
                    return 1;
                })
        );

        root.then(
            Commands.literal("vergizamani")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    MinecraftServer server = ctx.getSource().getServer();
                    List<ServerPlayer> players = server.getPlayerList().getPlayers();

                    Component title = Component.literal("VERGİ ZAMANI")
                        .withStyle(style -> style.withBold(true).withColor(ChatFormatting.RED));
                    for (ServerPlayer player : players) {
                        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 70, 20));
                        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title));
                    }

                    List<Component> logLines = new ArrayList<>();
                    for (ServerPlayer player : players) {
                        int amount = 5 + player.getRandom().nextInt(21);
                        int removed = removeItems(player, amount);
                        logLines.add(Component.literal(
                            player.getName().getString() + " — " + removed + " TL alındı.")
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)));
                    }

                    Component mainMsg = Component.literal("Maliye Bakanlığı vergi tahsilatı gerçekleştirdi.")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
                    for (ServerPlayer player : players) {
                        player.sendSystemMessage(mainMsg);
                        for (Component line : logLines) player.sendSystemMessage(line);
                    }

                    for (ServerPlayer player : players) {
                        player.connection.send(new ClientboundSoundPacket(
                            net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WITHER_SPAWN),
                            SoundSource.MASTER,
                            player.getX(), player.getY(), player.getZ(),
                            1.0f, 1.0f, player.getRandom().nextLong()));
                    }
                    return 1;
                })
        );

        root.then(
            Commands.literal("durum")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("[Bixis] Mevcut durum: "
                            + GameStateManager.INSTANCE.getState().name()), false);
                    return 1;
                })
        );

        root.then(
            Commands.literal("sifirla")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    GameStateManager.INSTANCE.setState(GameState.LOBI);
                    LobbyManager.INSTANCE.reset(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("[Bixis] Oyun LOBI'ye sıfırlandı."), true);
                    return 1;
                })
        );

        root.then(
            Commands.literal("takimsec")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        if (GameStateManager.INSTANCE.getState() != GameState.LOBI) {
                            ctx.getSource().sendFailure(Component.literal("⚠ Oyun zaten başladı.").withStyle(ChatFormatting.DARK_RED));
                            ServerPlayer ep = ctx.getSource().getPlayer();
                            if (ep != null) LobbyManager.playError(ep);
                            return 0;
                        }
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        LobbyManager.INSTANCE.joinTeam(player, IntegerArgumentType.getInteger(ctx, "takim"));
                        return 1;
                    }))
        );

        root.then(
            Commands.literal("hazir")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        if (GameStateManager.INSTANCE.getState() != GameState.LOBI) {
                            ctx.getSource().sendFailure(Component.literal("⚠ Oyun zaten başladı.").withStyle(ChatFormatting.DARK_RED));
                            ServerPlayer ep = ctx.getSource().getPlayer();
                            if (ep != null) LobbyManager.playError(ep);
                            return 0;
                        }
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        LobbyManager.INSTANCE.setReady(player, IntegerArgumentType.getInteger(ctx, "takim"));
                        return 1;
                    }))
        );

        root.then(
            Commands.literal("basla")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    if (GameStateManager.INSTANCE.getState() != GameState.LOBI) {
                        ctx.getSource().sendFailure(Component.literal("⚠ Oyun LOBI fazında değil.").withStyle(ChatFormatting.DARK_RED));
                        return 0;
                    }
                    MinecraftServer srv = ctx.getSource().getServer();
                    if (!LobbyManager.INSTANCE.allReadyCheck(srv)) {
                        ctx.getSource().sendFailure(Component.literal("⚠ Tüm takımlar hazır değil!").withStyle(ChatFormatting.DARK_RED));
                        return 0;
                    }
                    GameStateManager.INSTANCE.setState(GameState.GERI_SAYIM);
                    LobbyManager.INSTANCE.startCountdown(srv);
                    ctx.getSource().sendSuccess(() -> Component.literal("[Bixis] Geri sayım başladı!"), true);
                    return 1;
                })
        );

        root.then(
            Commands.literal("finish")
                .executes(ctx -> {
                    if (GameStateManager.INSTANCE.getState() != GameState.YARIS) {
                        ctx.getSource().sendFailure(Component.literal("⚠ Yarış fazında değilsiniz.").withStyle(ChatFormatting.DARK_RED));
                        return 0;
                    }
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    boolean recorded = RaceManager.INSTANCE.finishPlayer(player);
                    if (!recorded) {
                        // Zaten bitirmiş — sessizce yoksay (spec gereği)
                        return 0;
                    }
                    return 1;
                })
        );

        root.then(
            Commands.literal("arenaya_gec")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    if (GameStateManager.INSTANCE.getState() != GameState.HAZIRLIK_1) {
                        ctx.getSource().sendFailure(Component.literal("⚠ Oyun Hazırlık 1 fazında değil.").withStyle(ChatFormatting.DARK_RED));
                        return 0;
                    }
                    MinecraftServer srv = ctx.getSource().getServer();
                    Component broadcast = Component.literal("Hazırlık dönemi bitti, arenaya ışınlanıyorsunuz.")
                        .withStyle(ChatFormatting.YELLOW);
                    for (ServerPlayer p : srv.getPlayerList().getPlayers()) p.sendSystemMessage(broadcast);
                    ArenaManager.INSTANCE.teleportToArena(srv);
                    GameStateManager.INSTANCE.setState(GameState.HAZIRLIK_2);
                    ctx.getSource().sendSuccess(() -> Component.literal("[Bixis] Arenaya geçildi → HAZIRLIK_2."), true);
                    return 1;
                })
        );

        root.then(
            Commands.literal("kapisma_basla")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    if (GameStateManager.INSTANCE.getState() != GameState.HAZIRLIK_2) {
                        ctx.getSource().sendFailure(Component.literal("⚠ Oyun Hazırlık 2 fazında değil.").withStyle(ChatFormatting.DARK_RED));
                        return 0;
                    }
                    ArenaManager.INSTANCE.startFight(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal("[Bixis] Kapışma geri sayımı başladı."), true);
                    return 1;
                })
        );

        root.then(buildAdminNode());

        dispatcher.register(root);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  /bixis admin — tüm harita kurulum komutları
    // ────────────────────────────────────────────────────────────────────────

    private static LiteralArgumentBuilder<CommandSourceStack> buildAdminNode() {
        return Commands.literal("admin")
            .requires(src -> src.hasPermission(2))
            .then(buildSetNode())
            .then(buildListNode())
            .then(buildRemoveNode())
            .then(buildResetNode())
            .then(buildTpNode())
            .then(buildLoadmapNode());
    }

    // ── set ──────────────────────────────────────────────────────────────────

    private static LiteralArgumentBuilder<CommandSourceStack> buildSetNode() {
        return Commands.literal("set")
            .then(Commands.literal("race")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        int t = IntegerArgumentType.getInteger(ctx, "takim");
                        BixisRaceSpawnsConfig.setSpawn(t, p.getX(), p.getY(), p.getZ(),
                            p.getYRot(), dimOf(p));
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            formatSaved("race", t, p)), true);
                        return 1;
                    })))
            .then(Commands.literal("arena")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        int t = IntegerArgumentType.getInteger(ctx, "takim");
                        BixisArenaSpawnsConfig.setSpawn(t, p.getX(), p.getY(), p.getZ(),
                            p.getYRot(), dimOf(p));
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            formatSaved("arena", t, p)), true);
                        return 1;
                    })))
            .then(Commands.literal("checkpoint")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        int t = IntegerArgumentType.getInteger(ctx, "takim");
                        int idx = BixisCheckpointsConfig.addCheckpoint(t,
                            p.getX(), p.getY(), p.getZ(), p.getYRot(), dimOf(p));
                        final int fi = idx;
                        ctx.getSource().sendSuccess(() -> Component.literal(String.format(
                            "[Bixis] Takım %d checkpoint #%d kaydedildi: %s",
                            t, fi, formatCoords(p.getX(), p.getY(), p.getZ(), p.getYRot(), dimOf(p)))), true);
                        return 1;
                    })))
            .then(Commands.literal("racetime")
                .then(Commands.argument("dakika", IntegerArgumentType.integer(1, 60))
                    .executes(ctx -> {
                        int mins = IntegerArgumentType.getInteger(ctx, "dakika");
                        BixisRaceSettingsConfig.setRaceTimeMins(mins);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "[Bixis] Yarış süresi " + mins + " dakika olarak ayarlandı."), true);
                        return 1;
                    })))
            .then(Commands.literal("pvptime")
                .then(Commands.argument("dakika", IntegerArgumentType.integer(1, 60))
                    .executes(ctx -> {
                        int mins = IntegerArgumentType.getInteger(ctx, "dakika");
                        BixisRaceSettingsConfig.setPvpTimeMins(mins);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "[Bixis] PVP süresi " + mins + " dakika olarak ayarlandı."), true);
                        return 1;
                    })));
    }

    // ── list ─────────────────────────────────────────────────────────────────

    private static LiteralArgumentBuilder<CommandSourceStack> buildListNode() {
        return Commands.literal("list")
            .then(Commands.literal("race")
                .executes(ctx -> {
                    Map<Integer, SpawnPoint> all = BixisRaceSpawnsConfig.getAll();
                    ctx.getSource().sendSuccess(() -> Component.literal("[Bixis] Race spawn'ları:"), false);
                    for (int i = 1; i <= 4; i++) {
                        SpawnPoint sp = all.get(i);
                        final int fi = i;
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            sp != null
                                ? "  Takım " + fi + ": " + formatSpawnPoint(sp)
                                : "  Takım " + fi + ": (ayarlanmamış)"), false);
                    }
                    return 1;
                }))
            .then(Commands.literal("arena")
                .executes(ctx -> {
                    Map<Integer, SpawnPoint> all = BixisArenaSpawnsConfig.getAll();
                    ctx.getSource().sendSuccess(() -> Component.literal("[Bixis] Arena spawn'ları:"), false);
                    for (int i = 1; i <= 4; i++) {
                        SpawnPoint sp = all.get(i);
                        final int fi = i;
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            sp != null
                                ? "  Takım " + fi + ": " + formatSpawnPoint(sp)
                                : "  Takım " + fi + ": (ayarlanmamış)"), false);
                    }
                    return 1;
                }))
            .then(Commands.literal("checkpoint")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        int t = IntegerArgumentType.getInteger(ctx, "takim");
                        List<SpawnPoint> list = BixisCheckpointsConfig.getCheckpoints(t);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "[Bixis] Takım " + t + " checkpoint'leri (" + list.size() + " adet):"), false);
                        for (int i = 0; i < list.size(); i++) {
                            SpawnPoint sp = list.get(i);
                            final int fi = i + 1;
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "  #" + fi + ": " + formatSpawnPoint(sp)), false);
                        }
                        if (list.isEmpty()) {
                            ctx.getSource().sendSuccess(() -> Component.literal("  (kayıt yok)"), false);
                        }
                        return 1;
                    })));
    }

    // ── remove ───────────────────────────────────────────────────────────────

    private static LiteralArgumentBuilder<CommandSourceStack> buildRemoveNode() {
        return Commands.literal("remove")
            .then(Commands.literal("checkpoint")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .then(Commands.argument("sira", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int t = IntegerArgumentType.getInteger(ctx, "takim");
                            int s = IntegerArgumentType.getInteger(ctx, "sira");
                            if (!BixisCheckpointsConfig.removeCheckpoint(t, s)) {
                                ctx.getSource().sendFailure(Component.literal(
                                    "⚠ Takım " + t + "'de #" + s + " numaralı checkpoint yok.").withStyle(ChatFormatting.DARK_RED));
                                return 0;
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "[Bixis] Takım " + t + " checkpoint #" + s + " silindi."), true);
                            return 1;
                        }))));
    }

    // ── reset ────────────────────────────────────────────────────────────────

    private static LiteralArgumentBuilder<CommandSourceStack> buildResetNode() {
        return Commands.literal("reset")
            .then(Commands.literal("all")
                .executes(ctx -> {
                    BixisRaceSpawnsConfig.clearAll();
                    BixisArenaSpawnsConfig.clearAll();
                    BixisCheckpointsConfig.clearAll();
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "[Bixis] Tüm harita kayıtları (race, arena, checkpoint) silindi."), true);
                    return 1;
                }));
    }

    // ── tp ───────────────────────────────────────────────────────────────────

    private static LiteralArgumentBuilder<CommandSourceStack> buildTpNode() {
        return Commands.literal("tp")
            .then(Commands.literal("race")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        int t = IntegerArgumentType.getInteger(ctx, "takim");
                        Optional<SpawnPoint> sp = BixisRaceSpawnsConfig.getSpawn(t);
                        if (sp.isEmpty()) {
                            ctx.getSource().sendFailure(Component.literal("⚠ Takım " + t + " race spawn ayarlanmamış.").withStyle(ChatFormatting.DARK_RED));
                            return 0;
                        }
                        teleportAdmin(ctx.getSource().getPlayerOrException(), sp.get(), ctx.getSource().getServer());
                        return 1;
                    })))
            .then(Commands.literal("arena")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        int t = IntegerArgumentType.getInteger(ctx, "takim");
                        Optional<SpawnPoint> sp = BixisArenaSpawnsConfig.getSpawn(t);
                        if (sp.isEmpty()) {
                            ctx.getSource().sendFailure(Component.literal("⚠ Takım " + t + " arena spawn ayarlanmamış.").withStyle(ChatFormatting.DARK_RED));
                            return 0;
                        }
                        teleportAdmin(ctx.getSource().getPlayerOrException(), sp.get(), ctx.getSource().getServer());
                        return 1;
                    })))
            .then(Commands.literal("checkpoint")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .then(Commands.argument("sira", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int t = IntegerArgumentType.getInteger(ctx, "takim");
                            int s = IntegerArgumentType.getInteger(ctx, "sira");
                            List<SpawnPoint> list = BixisCheckpointsConfig.getCheckpoints(t);
                            if (s < 1 || s > list.size()) {
                                ctx.getSource().sendFailure(Component.literal("⚠ Takım " + t + "'de #" + s + " numaralı checkpoint yok.").withStyle(ChatFormatting.DARK_RED));
                                return 0;
                            }
                            teleportAdmin(ctx.getSource().getPlayerOrException(), list.get(s - 1), ctx.getSource().getServer());
                            return 1;
                        }))));
    }

    // ── loadmap ──────────────────────────────────────────────────────────────

    private static LiteralArgumentBuilder<CommandSourceStack> buildLoadmapNode() {
        return Commands.literal("loadmap")
            .then(Commands.argument("isim", StringArgumentType.word())
                .executes(ctx -> {
                    String mapName = StringArgumentType.getString(ctx, "isim");
                    MinecraftServer server = ctx.getSource().getServer();
                    // Forge config dizinini server instance'ından al
                    Path configDir = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
                    Path mapDir = configDir.resolve("maps").resolve(mapName);

                    if (!Files.isDirectory(mapDir)) {
                        ctx.getSource().sendFailure(Component.literal(
                            "⚠ Harita bulunamadı: config/maps/" + mapName + "/").withStyle(ChatFormatting.DARK_RED));
                        return 0;
                    }

                    Path raceFile  = mapDir.resolve("race-spawns.json");
                    Path arenaFile = mapDir.resolve("arena-spawns.json");
                    Path cpFile    = mapDir.resolve("checkpoints.json");

                    // En az bir dosya olmalı
                    if (!Files.exists(raceFile) && !Files.exists(arenaFile) && !Files.exists(cpFile)) {
                        ctx.getSource().sendFailure(Component.literal(
                            "⚠ Harita klasöründe hiçbir config dosyası yok: " + mapName).withStyle(ChatFormatting.DARK_RED));
                        return 0;
                    }

                    List<String> loaded = new ArrayList<>();
                    if (Files.exists(raceFile)) {
                        copyJsonFile(raceFile, configDir.resolve("bixis-race-spawns.json"));
                        BixisRaceSpawnsConfig.loadFrom(configDir.resolve("bixis-race-spawns.json"));
                        loaded.add("race");
                    }
                    if (Files.exists(arenaFile)) {
                        copyJsonFile(arenaFile, configDir.resolve("bixis-arena-spawns.json"));
                        BixisArenaSpawnsConfig.loadFrom(configDir.resolve("bixis-arena-spawns.json"));
                        loaded.add("arena");
                    }
                    if (Files.exists(cpFile)) {
                        copyJsonFile(cpFile, configDir.resolve("bixis-checkpoints.json"));
                        BixisCheckpointsConfig.loadFrom(configDir.resolve("bixis-checkpoints.json"));
                        loaded.add("checkpoint");
                    }

                    String summary = String.join(", ", loaded);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "[Bixis] Harita '" + mapName + "' yüklendi (" + summary + ")."), true);
                    return 1;
                }));
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Dahili yardımcılar
    // ────────────────────────────────────────────────────────────────────────

    private static void teleportAdmin(ServerPlayer player, SpawnPoint sp, MinecraftServer server) {
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(sp.dimension()));
        ServerLevel level = server.getLevel(dimKey);
        if (level == null) {
            player.sendSystemMessage(Component.literal(
                "⚠ Dimension bulunamadı: " + sp.dimension()).withStyle(ChatFormatting.DARK_RED));
            return;
        }
        player.teleportTo(level, sp.x(), sp.y(), sp.z(), sp.yaw(), 0f);
    }

    private static void copyJsonFile(Path src, Path dst) throws RuntimeException {
        try {
            Files.copy(src, dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Dosya kopyalanamadı: " + src, e);
        }
    }

    private static String dimOf(ServerPlayer player) {
        return player.level().dimension().location().toString();
    }

    private static String formatCoords(double x, double y, double z, float yaw, String dim) {
        return String.format("%.1f %.1f %.1f (yaw:%.1f) [%s]", x, y, z, yaw, dim);
    }

    private static String formatSpawnPoint(SpawnPoint sp) {
        return formatCoords(sp.x(), sp.y(), sp.z(), sp.yaw(), sp.dimension());
    }

    private static String formatSaved(String type, int teamNum, ServerPlayer p) {
        return String.format("[Bixis] Takım %d %s spawn kaydedildi: %s",
            teamNum, type, formatCoords(p.getX(), p.getY(), p.getZ(), p.getYRot(), dimOf(p)));
    }

    /** Oyuncunun envanterinden en fazla {@code amount} adet turk_lirasi siler, silinen miktarı döner. */
    private static int removeItems(ServerPlayer player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(BixisItems.TURK_LIRASI.get())) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }
        return amount - remaining;
    }

    private BixisCommand() {}
}
