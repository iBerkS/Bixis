package com.bixis.bixismod.weapon;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gaddare ve Ateş Asası mekaniklerini event tabanlı yönetir.
 * <ul>
 *   <li>Gaddare: hedef maks canının %30'undaysa hasar x2</li>
 *   <li>Ateş Asası: cooldown bitince actionbar, doluyorsa geri sayım</li>
 * </ul>
 */
public class WeaponEventHandler {

    /** Önceki tick'te cooldown'da olan oyuncuların durumunu izler. */
    private final Map<UUID, Boolean> wasOnCooldown = new HashMap<>();

    /** Ateş Asası hazır olunca actionbar'ın kaç tick daha gösterileceği. */
    private final Map<UUID, Integer> readyDisplayTicks = new HashMap<>();

    // -------------------------------------------------------------------------
    // Gaddare — x2 finisher hasarı
    // -------------------------------------------------------------------------

    /**
     * Saldırıyı yapan oyuncu elinde GaddareItem tutuyorsa ve hedef %30 canın
     * altındaysa hasarı iki katına çıkarır.
     */
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player.getMainHandItem().getItem() instanceof GaddareItem)) return;

        LivingEntity target = event.getEntity();
        if (target.getHealth() / target.getMaxHealth() <= 0.30f) {
            event.setAmount(event.getAmount() * 2f);
        }
    }

    // -------------------------------------------------------------------------
    // Ateş Asası — actionbar bildirimi
    // -------------------------------------------------------------------------

    /** Oyuncu mainhand'inde Ateş Asası tutuyorsa cooldown durumunu gösterir. */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (!(player.getMainHandItem().getItem() instanceof AtesAsasiItem atesAsasi)) {
            wasOnCooldown.remove(player.getUUID());
            readyDisplayTicks.remove(player.getUUID());
            return;
        }

        boolean onCooldown = player.getCooldowns().isOnCooldown(atesAsasi);
        boolean wasCooling = wasOnCooldown.getOrDefault(player.getUUID(), false);
        wasOnCooldown.put(player.getUUID(), onCooldown);

        if (onCooldown) {
            float pct = player.getCooldowns().getCooldownPercent(atesAsasi, 0f);
            int remaining = (int) Math.ceil(AtesAsasiItem.COOLDOWN_TICKS * pct / 20.0);
            serverPlayer.displayClientMessage(
                Component.literal("Ates Asasi doluyor... (" + remaining + "sn)"), true);
            readyDisplayTicks.remove(player.getUUID());
        } else if (wasCooling) {
            // Cooldown yeni bitti — 3 saniye (60 tick) hazır mesajı göster
            readyDisplayTicks.put(player.getUUID(), 60);
        }

        int remaining = readyDisplayTicks.getOrDefault(player.getUUID(), 0);
        if (remaining > 0) {
            serverPlayer.displayClientMessage(Component.literal("✦ Ates Asasi hazir!"), true);
            readyDisplayTicks.put(player.getUUID(), remaining - 1);
        }
    }
}
