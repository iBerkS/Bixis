package com.bixis.bixismod.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Gaddare — ağır ve yavaş silah.
 * Vuruşta %15 ihtimalle Slowness II (2 sn) uygular.
 * Hedef maks canının %30'undaysa verilen hasar x2 olur (WeaponEventHandler).
 */
public class GaddareItem extends SwordItem {

    /** Yavaşlama tetiklenme olasılığı. */
    private static final float SLOW_CHANCE = 0.15f;
    /** Slowness II süresi (2 saniye). */
    private static final int SLOW_TICKS = 40;

    /**
     * @param tier           silah kademesi
     * @param attackDamageMod hasar modifier'ı
     * @param attackSpeedMod  hız modifier'ı
     * @param props          item özellikleri
     */
    public GaddareItem(Tier tier, int attackDamageMod, float attackSpeedMod, Properties props) {
        super(tier, attackDamageMod, attackSpeedMod, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Her vuruşta %15 ihtimalle yavaşlatır.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("Düşman %30 canın altındayken hasar").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("iki katına çıkar.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /** Vuruş sonrası %15 ihtimalle Slowness II efekti uygular. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && attacker.level().random.nextFloat() < SLOW_CHANCE) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 1));
        }
        return result;
    }
}
