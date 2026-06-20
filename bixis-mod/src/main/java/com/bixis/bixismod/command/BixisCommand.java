package com.bixis.bixismod.command;

import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.minigame.GameState;
import com.bixis.bixismod.minigame.GameStateManager;
import com.bixis.bixismod.minigame.LobbyManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** /bixis komutlarını kaydeden sınıf. */
public final class BixisCommand {

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
                        () -> Component.literal("Bixis Mod aktif!"), false
                    );
                    return 1;
                })
        );

        root.then(
            Commands.literal("vergizamani")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    MinecraftServer server = ctx.getSource().getServer();
                    List<ServerPlayer> players = server.getPlayerList().getPlayers();

                    // 1. Title gönder
                    Component title = Component.literal("VERGİ ZAMANI")
                        .withStyle(style -> style.withBold(true).withColor(net.minecraft.ChatFormatting.RED));
                    for (ServerPlayer player : players) {
                        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 70, 20));
                        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title));
                    }

                    // 2. Her oyuncudan random TL sil, log tut
                    List<Component> logLines = new ArrayList<>();
                    for (ServerPlayer player : players) {
                        int amount = 5 + player.getRandom().nextInt(21); // 5-25
                        int removed = removeItems(player, amount);
                        Component logLine = Component.literal(
                            player.getName().getString() + " — " + removed + " TL alındı."
                        ).withStyle(Style.EMPTY.withColor(net.minecraft.ChatFormatting.GRAY).withItalic(true));
                        logLines.add(logLine);
                    }

                    // 3. Chat mesajı + log tüm oyunculara
                    Component mainMsg = Component.literal("Maliye Bakanlığı vergi tahsilatı gerçekleştirdi.")
                        .withStyle(Style.EMPTY.withColor(net.minecraft.ChatFormatting.GRAY).withItalic(true));
                    for (ServerPlayer player : players) {
                        player.sendSystemMessage(mainMsg);
                        for (Component line : logLines) {
                            player.sendSystemMessage(line);
                        }
                    }

                    // 4. Wither spawn sesi tüm oyunculara
                    for (ServerPlayer player : players) {
                        player.connection.send(new ClientboundSoundPacket(
                            net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WITHER_SPAWN),
                            SoundSource.MASTER,
                            player.getX(), player.getY(), player.getZ(),
                            1.0f, 1.0f, player.getRandom().nextLong()
                        ));
                    }

                    return 1;
                })
        );

        root.then(
            Commands.literal("durum")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("[Bixis] Mevcut durum: "
                            + GameStateManager.INSTANCE.getState().name()), false
                    );
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
                        () -> Component.literal("[Bixis] Oyun LOBI'ye sıfırlandı."), true
                    );
                    return 1;
                })
        );

        root.then(
            Commands.literal("takimsec")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        if (GameStateManager.INSTANCE.getState() != GameState.LOBI) {
                            ctx.getSource().sendFailure(Component.literal("⚠ Oyun zaten başladı.").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                            ServerPlayer ep = ctx.getSource().getPlayer();
                            if (ep != null) LobbyManager.playError(ep);
                            return 0;
                        }
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int takim = IntegerArgumentType.getInteger(ctx, "takim");
                        LobbyManager.INSTANCE.joinTeam(player, takim);
                        return 1;
                    }))
        );

        root.then(
            Commands.literal("hazir")
                .then(Commands.argument("takim", IntegerArgumentType.integer(1, 4))
                    .executes(ctx -> {
                        if (GameStateManager.INSTANCE.getState() != GameState.LOBI) {
                            ctx.getSource().sendFailure(Component.literal("⚠ Oyun zaten başladı.").withStyle(net.minecraft.ChatFormatting.DARK_RED));
                            ServerPlayer ep = ctx.getSource().getPlayer();
                            if (ep != null) LobbyManager.playError(ep);
                            return 0;
                        }
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int takim = IntegerArgumentType.getInteger(ctx, "takim");
                        LobbyManager.INSTANCE.setReady(player, takim);
                        return 1;
                    }))
        );

        dispatcher.register(root);
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
