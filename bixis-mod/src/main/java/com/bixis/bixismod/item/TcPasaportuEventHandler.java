package com.bixis.bixismod.item;

import com.bixis.bixismod.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Ölüm anında TC Pasaportu'nun Totem of Undying benzeri etkisini uygular.
 */
public class TcPasaportuEventHandler {

    /** Ölüm iptal edilip efektler uygulanır; pasaport bir adet tüketilir. */
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        ItemStack passport = findPassport(player);
        if (passport == null) return;

        event.setCanceled(true);
        player.setHealth(1.0f);
        player.removeAllEffects();

        int ticks = Constants.TC_PASAPORTU_EFFECT_TICKS;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,   ticks, Constants.TC_PASAPORTU_SPEED_AMP));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP,             ticks, Constants.TC_PASAPORTU_JUMP_AMP));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,     ticks, Constants.TC_PASAPORTU_REGEN_AMP));

        player.invulnerableTime = 20;
        passport.shrink(1);

        String msg = player.getDisplayName().getString() + " pasaportunu kullandı. İyi yolculuklar!";
        player.getServer().getPlayerList().broadcastSystemMessage(Component.literal(msg), false);
    }

    /**
     * Offhand'a önce bakar, yoksa ana ele bakar.
     * @return pasaport stack'i, bulunmazsa null
     */
    private ItemStack findPassport(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() == BixisItems.TC_PASAPORTU.get()) {
            return offhand;
        }
        ItemStack mainhand = player.getMainHandItem();
        if (!mainhand.isEmpty() && mainhand.getItem() == BixisItems.TC_PASAPORTU.get()) {
            return mainhand;
        }
        return null;
    }
}
