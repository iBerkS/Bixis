package com.bixis.bixismod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Her tick'te oyuncunun elindeki item'ı kontrol eder.
 * Main hand veya off hand'de mutlak_butlan varsa Resistance II uygular; yoksa kaldırır.
 */
public class MutlakButlanEventHandler {

    /** @param event tick sonu tetiklenir (server tarafı) */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;

        Player player = event.player;
        boolean hasItem = hasInEitherHand(player);
        boolean hasEffect = player.hasEffect(MobEffects.DAMAGE_RESISTANCE);

        if (hasItem && !hasEffect) {
            player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1, false, false, true));
        } else if (!hasItem && hasEffect) {
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        }
    }

    private boolean hasInEitherHand(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off  = player.getOffhandItem();
        return (!main.isEmpty() && main.getItem() == BixisItems.MUTLAK_BUTLAN.get())
            || (!off.isEmpty()  && off.getItem()  == BixisItems.MUTLAK_BUTLAN.get());
    }
}
