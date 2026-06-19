package com.bixis.bixismod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Faiz Kalkanı — elde tutulunca Absorption biriktirir (FaizKalkaniEventHandler yönetir).
 * Tüketilmez, stack: 1.
 */
public class FaizKalkaniItem extends Item {

    public FaizKalkaniItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Hasar almadığın her 60 saniyede 1 kalp biriktirir, en fazla 5 kalp.")
            .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Merkez Bankası faizi koruyor.")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
