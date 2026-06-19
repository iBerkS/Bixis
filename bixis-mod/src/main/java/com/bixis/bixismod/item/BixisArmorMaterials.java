package com.bixis.bixismod.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Bixis özel zırh materyalleri.
 */
public enum BixisArmorMaterials implements ArmorMaterial {

    /**
     * Fenerbahçe Forması — netherite'ten bir tık güçlü chestplate.
     * Zırh: 9 (netherite 8), tokluğu: 4, KBR: 0.1, enchant: 15.
     * Ses: deri zırh giyme sesi.
     */
    FENERBAHCE_FORMA("bixis:fenerbahce_forma",
        new int[]{3, 6, 9, 3},   // feet, legs, chest, head
        15,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        4.0f,
        0.1f);

    // -------------------------------------------------------------------------

    private static final int[] DURABILITY_BASE = {13, 15, 16, 11};

    private final String name;
    private final int[] defense;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;

    BixisArmorMaterials(String name, int[] defense, int enchantability,
                        SoundEvent equipSound, float toughness, float knockbackResistance) {
        this.name                = name;
        this.defense             = defense;
        this.enchantability      = enchantability;
        this.equipSound          = equipSound;
        this.toughness           = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return DURABILITY_BASE[type.getSlot().getIndex()] * 37;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return defense[type.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() { return enchantability; }

    @Override
    public SoundEvent getEquipSound() { return equipSound; }

    @Override
    public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }

    @Override
    public String getName() { return name; }

    @Override
    public float getToughness() { return toughness; }

    @Override
    public float getKnockbackResistance() { return knockbackResistance; }
}
