package com.bixis.bixismod.weapon;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.InteractionHand;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Multimap;

/**
 * Ateş Asası — sol vuruş hasarı 5, sağ tıkta 6 blok yarıçapta entity'leri tutuşturur.
 * Sağ tık sonrası 8 saniye (160 tick) cooldown.
 * Actionbar bildirimi WeaponEventHandler tarafından yönetilir.
 */
public class AtesAsasiItem extends Item {

    /** Cooldown süresi tick olarak (8 saniye). */
    public static final int COOLDOWN_TICKS = 160;
    /** Ateş menzili blok olarak. */
    private static final double FIRE_RADIUS = 6.0;
    /** Ateş süresi saniye olarak. */
    private static final int FIRE_SECONDS = 5;

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("f1a2b3c4-d5e6-7890-abcd-ef1234567890");
    private static final UUID ATTACK_SPEED_UUID  = UUID.fromString("a0b1c2d3-e4f5-6789-0abc-def123456789");

    /** @param props item özellikleri */
    public AtesAsasiItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Sağ tıkla 6 blok yarıçapındaki düşmanları").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("tutuşturur. 8 saniye şarj süresi.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /**
     * Mainhand'de: +4 attack damage (1 base + 4 = 5 toplam), -2.4 speed (sword gibi).
     */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(ATTACK_DAMAGE_UUID, "Weapon modifier", 4.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(ATTACK_SPEED_UUID, "Weapon modifier", -2.4, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    /**
     * Sağ tıkta 4 blok yarıçapta tüm düşman entity'leri tutuşturur.
     * Cooldown bitince WeaponEventHandler actionbar mesajı gösterir.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && !player.getCooldowns().isOnCooldown(this)) {
            AABB area = player.getBoundingBox().inflate(FIRE_RADIUS);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && !e.isAlliedTo(player));
            for (LivingEntity target : targets) {
                target.setSecondsOnFire(FIRE_SECONDS);
            }
            level.playSound(null, player.blockPosition(),
                BixisSounds.ATES_ASASI_CAST.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
