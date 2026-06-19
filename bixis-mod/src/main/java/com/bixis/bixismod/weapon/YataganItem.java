package com.bixis.bixismod.weapon;

import com.bixis.bixismod.effect.BixisEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Yatağan — her vuruşta %8 ihtimalle Kanama II uygular (140 tick / 7 saniye).
 */
public class YataganItem extends SwordItem {

    /** Kanama tetiklenme olasılığı. */
    private static final float KANAMA_CHANCE = 0.08f;
    /** Kanama süresi tick olarak (7 saniye). */
    private static final int KANAMA_TICKS = 140;

    /**
     * @param tier           silah kademesi
     * @param attackDamageMod hasar modifier'ı
     * @param attackSpeedMod  hız modifier'ı
     * @param props          item özellikleri
     */
    public YataganItem(Tier tier, int attackDamageMod, float attackSpeedMod, Properties props) {
        super(tier, attackDamageMod, attackSpeedMod, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Her vuruşta rakibe %5 ihtimalle 7 saniyelik").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("kanama hasarı verir.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /** Vuruş sonrası %8 ihtimalle Kanama II efekti uygular. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && attacker.level().random.nextFloat() < KANAMA_CHANCE) {
            target.addEffect(new MobEffectInstance(BixisEffects.KANAMA.get(), KANAMA_TICKS, 1));
        }
        return result;
    }
}
