package com.bixis.bixismod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Faiz Kalkanı mekaniği:
 * - Elde tutulunca her 1200 tick'te iç sayaç +1 (max 5), Absorption sayaç seviyesinde uygulanır.
 * - Item elden çıkınca Absorption silinir, sayaç ve tick sayacı sıfırlanır.
 * - Hasar alınca mevcut Absorption miktarı sayaca senkronize edilir.
 */
public class FaizKalkaniEventHandler {

    private static final int TICK_INTERVAL = 400; // 20 saniye
    private static final int MAX_STACKS    = 5;

    /** UUID → birikim sayacı (0-5) */
    private final Map<UUID, Integer> stackCount = new HashMap<>();
    /** UUID → son birikim tickinden bu yana geçen tick */
    private final Map<UUID, Integer> ticksSince = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;

        Player player = event.player;
        UUID id = player.getUUID();

        if (!hasInEitherHand(player)) {
            // Item elde değil — temizle
            if (stackCount.containsKey(id)) {
                stackCount.remove(id);
                ticksSince.remove(id);
                player.removeEffect(MobEffects.ABSORPTION);
            }
            return;
        }

        int stacks = stackCount.getOrDefault(id, 0);
        int ticks  = ticksSince.getOrDefault(id, 0) + 1;

        if (ticks >= TICK_INTERVAL && stacks < MAX_STACKS) {
            stacks++;
            ticks = 0;
            applyAbsorption(player, stacks);
        }

        stackCount.put(id, stacks);
        ticksSince.put(id, ticks);
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (!hasInEitherHand(player)) return;

        // Hasar işlendikten sonra mevcut Absorption miktarını sayaca senkronize et
        // (LivingDamageEvent hasar uygulanmadan önce tetiklenir, post-event için sonraki tick'te okuyoruz)
        UUID id = player.getUUID();
        // Absorption float değerini kalp sayısına çevir (2 absorption point = 1 kalp)
        float absHp = player.getAbsorptionAmount() - event.getAmount();
        int newStacks = Math.max(0, Math.round(absHp / 2.0f));
        stackCount.put(id, Math.min(newStacks, MAX_STACKS));
        // Tick sayacını sıfırla — hasar sonrası birikim baştan başlasın
        ticksSince.put(id, 0);
    }

    private void applyAbsorption(Player player, int stacks) {
        // Absorption amplifier = stacks - 1 (amplifier 0 = Absorption I = 2 kalp)
        player.addEffect(new MobEffectInstance(
            MobEffects.ABSORPTION, Integer.MAX_VALUE, stacks - 1, false, false, true));
    }

    private boolean hasInEitherHand(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off  = player.getOffhandItem();
        return (!main.isEmpty() && main.getItem() == BixisItems.FAIZ_KALKANI.get())
            || (!off.isEmpty()  && off.getItem()  == BixisItems.FAIZ_KALKANI.get());
    }
}
