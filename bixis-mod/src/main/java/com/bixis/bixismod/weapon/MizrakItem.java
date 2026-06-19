package com.bixis.bixismod.weapon;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

/**
 * Mızrak — +2 entity reach, sağ tıkta fırlatılır ve geri toplanabilir.
 * Fırlatılan entity ThrowableItemProjectile tabanlıdır; item modeli render edilir.
 */
public class MizrakItem extends SwordItem {

    private static final UUID REACH_UUID = UUID.fromString("a3f1c2e4-5b67-4d89-b0a1-2c3d4e5f6789");
    private static final float THROW_SPEED = 2.5f;

    private final double thrownDamage;

    /**
     * @param tier            silah kademesi
     * @param attackDamageMod hasar modifier'ı
     * @param attackSpeedMod  hız modifier'ı
     * @param thrownDamage    fırlatıldığında verilen hasar
     * @param props           item özellikleri
     */
    public MizrakItem(Tier tier, int attackDamageMod, float attackSpeedMod,
                      double thrownDamage, Properties props) {
        super(tier, attackDamageMod, attackSpeedMod, props);
        this.thrownDamage = thrownDamage;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Menzili uzundur. Sağ tıkla fırlatılabilir.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /** Mainhand'deyken +2 entity reach ekler. */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getAttributeModifiers(slot, stack));
        builder.put(ForgeMod.ENTITY_REACH.get(),
            new AttributeModifier(REACH_UUID, "Mizrak extra reach", 2.0, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    /**
     * Sağ tıkta mızrağı fırlatır; inventory'den bir adet kaldırılır.
     * Projectile toplandığında item geri döner.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            MizrakProjectileEntity entity = new MizrakProjectileEntity(level, player, stack.copy(), thrownDamage);
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, THROW_SPEED, 1.0f);
            level.addFreshEntity(entity);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
