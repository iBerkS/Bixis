package com.bixis.bixismod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Fenerbahçe Forması giyilince Strength I efekti verir; çıkarılınca kaldırır.
 */
public final class FenerbahceFormaEventHandler {

    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.CHEST) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        boolean hadForma = isForma(event.getFrom().getItem());
        boolean hasForma = isForma(event.getTo().getItem());

        if (!hadForma && hasForma) {
            entity.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 0, false, false, true));
        } else if (hadForma && !hasForma) {
            entity.removeEffect(MobEffects.DAMAGE_BOOST);
        }
    }

    private boolean isForma(net.minecraft.world.item.Item item) {
        return item instanceof ArmorItem armor
            && armor.getMaterial() == BixisArmorMaterials.FENERBAHCE_FORMA;
    }
}
