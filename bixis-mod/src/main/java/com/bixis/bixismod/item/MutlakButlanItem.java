package com.bixis.bixismod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Mutlak Butlan — elde tutulduğu sürece Resistance II verir (efekti MutlakButlanEventHandler yönetir).
 * Tüketilmez, stack: 1.
 */
public class MutlakButlanItem extends Item {

    public MutlakButlanItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Elde tutulduğu zaman Direnç II verir.")
            .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Anayasa Mahkemesi hasar verilmesini iptal etti.")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
