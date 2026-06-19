package com.bixis.bixismod.item;

import com.bixis.bixismod.BixisMod;
import com.bixis.bixismod.npc.BixisEntities;
import com.bixis.bixismod.weapon.AtesAsasiItem;
import com.bixis.bixismod.weapon.BixisSounds;
import com.bixis.bixismod.weapon.FullAutoGunItem;
import com.bixis.bixismod.weapon.GaddareItem;
import com.bixis.bixismod.weapon.GunItem;
import com.bixis.bixismod.weapon.MizrakItem;
import com.bixis.bixismod.weapon.YataganItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Tüm bixis item kayıtları.
 *
 * <p>Hasar hesabı: toplam = 1 (player base) + tier.bonus + attackDamageMod</p>
 * <ul>
 *   <li>IRON tier bonus = 2</li>
 *   <li>DIAMOND tier bonus = 3</li>
 *   <li>NETHERITE tier bonus = 4</li>
 * </ul>
 */
public final class BixisItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, BixisMod.MOD_ID);

    // =========================================================================
    // Para birimi
    // =========================================================================

    /** Türk Lirası para birimi item'ı. Stack size 64. */
    public static final RegistryObject<Item> TURK_LIRASI = ITEMS.register(
        "turk_lirasi",
        () -> new Item(new Item.Properties().stacksTo(64))
    );

    // =========================================================================
    // Villa Hakan shop item'ları
    // =========================================================================

    /** Fenerbahçe Forması — netherite'ten güçlü custom chestplate, giyince Strength I. */
    public static final RegistryObject<Item> FENERBAHCE_FORMA = ITEMS.register(
        "fenerbahce_forma",
        () -> new ArmorItem(BixisArmorMaterials.FENERBAHCE_FORMA, ArmorItem.Type.CHESTPLATE,
                new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)) {
            @Override
            public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                super.appendHoverText(stack, level, tooltip, flag);
                tooltip.add(Component.literal("İYYYEAHHHHH").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    );

    /**
     * M4 otomatik tüfek — tam otomatik, 30 mermi şarjör, 2.5 sn reload, hasar 4.
     * Her 2 tick'te bir ateş (saniyede 10 mermi). Sağ tık basılı tutunca ateş eder.
     */
    public static final RegistryObject<Item> M4 = ITEMS.register(
        "m4",
        () -> new FullAutoGunItem(30, 50, 3.5f, 6.0f, 2, "M4",
            BixisSounds.M4_FIRE, BixisSounds.M4_EMPTY,
            BixisSounds.M4_EQUIP, BixisSounds.M4_RELOAD,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    /**
     * AWP keskin nişancı tüfeği — yarı otomatik, 5 mermi şarjör, 3 sn reload, hasar 20.
     * Her ateş sonrası 1 sn (20 tick) cooldown.
     */
    public static final RegistryObject<Item> AWP = ITEMS.register(
        "awp",
        () -> new GunItem(5, 60, 3.5f, 20.0f, 20, "AWP",
            BixisSounds.AWP_FIRE, BixisSounds.AWP_EMPTY,
            BixisSounds.AWP_EQUIP, BixisSounds.AWP_RELOAD,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE))
    );

    /**
     * Desert Eagle tabancası — yarı otomatik, 7 mermi şarjör, 1.5 sn reload, hasar 8.
     * Şarjör durumu BixisAmmo NBT tag'inde saklanır.
     */
    public static final RegistryObject<Item> DESERT_EAGLE = ITEMS.register(
        "desert_eagle",
        () -> new GunItem(7, 30, 3.5f, 8.0f, 5, "Desert Eagle",
            BixisSounds.DESERT_EAGLE_FIRE, BixisSounds.DESERT_EAGLE_EMPTY,
            BixisSounds.DESERT_EAGLE_EQUIP, BixisSounds.DESERT_EAGLE_RELOAD,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    /** Mermi — Villa Hakan'dan 1 TL'ye 32 adet satılır. */
    public static final RegistryObject<Item> MERMI = ITEMS.register(
        "mermi",
        () -> new Item(new Item.Properties().stacksTo(64))
    );

    /**
     * Fenerbahçe kılıcı — hasar: 10, hız: diamond sword (-2.4).
     * Diamond tier (bonus 3): attackDamageMod = 10 - 1 - 3 = 6.
     */
    public static final RegistryObject<Item> FENERBAHCE_KILICI = ITEMS.register(
        "fenerbahce_kilici",
        () -> new SwordItem(Tiers.DIAMOND, 6, -2.4f, new Item.Properties().stacksTo(1)) {
            @Override
            public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                super.appendHoverText(stack, level, tooltip, flag);
                tooltip.add(Component.literal("Sarı lacivert kadar sade ve asil.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    );

    /** Faiz Kalkanı — elde tutulunca Absorption biriktirir (FaizKalkaniEventHandler). */
    public static final RegistryObject<Item> FAIZ_KALKANI = ITEMS.register(
        "faiz_kalkani",
        () -> new FaizKalkaniItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );

    /** TC Pasaportu — ölümde Totem of Undying benzeri etki yapar. */
    public static final RegistryObject<Item> TC_PASAPORTU = ITEMS.register(
        "tc_pasaportu",
        () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)) {
            @Override
            public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.literal("Ölümsüzlük totemi gibi çalışır.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                tooltip.add(Component.literal("İyi yolculuklar!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    );

    // =========================================================================
    // HANÇER — çok hızlı saldırı (attackSpeedMod = 0.0f → 4 hits/sec)
    // Hasar: iron=4, diamond=5, netherite=6 | mod = hasar - 1 - tier.bonus
    // =========================================================================

    /** Demir Hançer — hasar: 4, hız: çok hızlı. */
    public static final RegistryObject<Item> IRON_HANCER = ITEMS.register(
        "iron_hancer",
        () -> new SwordItem(Tiers.IRON, 1, 0.0f, new Item.Properties().stacksTo(1)) {
            @Override public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> t, TooltipFlag f) {
                t.add(Component.literal("Saldırı hızı çok yüksektir.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    );

    /** Elmas Hançer — hasar: 5, hız: çok hızlı. */
    public static final RegistryObject<Item> DIAMOND_HANCER = ITEMS.register(
        "diamond_hancer",
        () -> new SwordItem(Tiers.DIAMOND, 1, 0.0f, new Item.Properties().stacksTo(1)) {
            @Override public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> t, TooltipFlag f) {
                t.add(Component.literal("Saldırı hızı çok yüksektir.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    );

    /** Netherite Hançer — hasar: 6, hız: çok hızlı. */
    public static final RegistryObject<Item> NETHERITE_HANCER = ITEMS.register(
        "netherite_hancer",
        () -> new SwordItem(Tiers.NETHERITE, 1, 0.0f, new Item.Properties().stacksTo(1)) {
            @Override public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> t, TooltipFlag f) {
                t.add(Component.literal("Saldırı hızı çok yüksektir.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    );

    // =========================================================================
    // MIZRAK — +2 reach, sağ tıkta fırlatılabilir
    // Hasar: iron=6, diamond=6, netherite=7 | attackSpeedMod: -2.4
    // =========================================================================

    /** Demir Mızrak — hasar: 6, reach +2. */
    public static final RegistryObject<Item> IRON_MIZRAK = ITEMS.register(
        "iron_mizrak",
        () -> new MizrakItem(Tiers.IRON, 3, -2.4f, 6.0, new Item.Properties().stacksTo(1))
    );

    /** Elmas Mızrak — hasar: 6, reach +2. */
    public static final RegistryObject<Item> DIAMOND_MIZRAK = ITEMS.register(
        "diamond_mizrak",
        () -> new MizrakItem(Tiers.DIAMOND, 2, -2.4f, 6.0, new Item.Properties().stacksTo(1))
    );

    /** Netherite Mızrak — hasar: 7, reach +2. */
    public static final RegistryObject<Item> NETHERITE_MIZRAK = ITEMS.register(
        "netherite_mizrak",
        () -> new MizrakItem(Tiers.NETHERITE, 2, -2.4f, 7.0, new Item.Properties().stacksTo(1))
    );

    // =========================================================================
    // YATAĞAN — %8 Kanama II şansı (140 tick), normal hız (-2.4)
    // Hasar: iron=6, diamond=7, netherite=8
    // =========================================================================

    /** Demir Yatağan — hasar: 6, %8 Kanama II şansı. */
    public static final RegistryObject<Item> IRON_YATAGAN = ITEMS.register(
        "iron_yatagan",
        () -> new YataganItem(Tiers.IRON, 3, -2.4f, new Item.Properties().stacksTo(1))
    );

    /** Elmas Yatağan — hasar: 7, %8 Kanama II şansı. */
    public static final RegistryObject<Item> DIAMOND_YATAGAN = ITEMS.register(
        "diamond_yatagan",
        () -> new YataganItem(Tiers.DIAMOND, 3, -2.4f, new Item.Properties().stacksTo(1))
    );

    /** Netherite Yatağan — hasar: 8, %8 Kanama II şansı. */
    public static final RegistryObject<Item> NETHERITE_YATAGAN = ITEMS.register(
        "netherite_yatagan",
        () -> new YataganItem(Tiers.NETHERITE, 3, -2.4f, new Item.Properties().stacksTo(1))
    );

    // =========================================================================
    // GADDARE — ağır (hız -3.0), %15 Slowness II, x2 hasar <%30 canda
    // Hasar: iron=9, diamond=9, netherite=9
    // =========================================================================

    /** Demir Gaddare — hasar: 9, hız: yavaş, %15 Slowness II. */
    public static final RegistryObject<Item> IRON_GADDARE = ITEMS.register(
        "iron_gaddare",
        () -> new GaddareItem(Tiers.IRON, 6, -3.0f, new Item.Properties().stacksTo(1))
    );

    /** Elmas Gaddare — hasar: 9, hız: yavaş, %15 Slowness II. */
    public static final RegistryObject<Item> DIAMOND_GADDARE = ITEMS.register(
        "diamond_gaddare",
        () -> new GaddareItem(Tiers.DIAMOND, 5, -3.0f, new Item.Properties().stacksTo(1))
    );

    /** Netherite Gaddare — hasar: 9, hız: yavaş, %15 Slowness II. */
    public static final RegistryObject<Item> NETHERITE_GADDARE = ITEMS.register(
        "netherite_gaddare",
        () -> new GaddareItem(Tiers.NETHERITE, 4, -3.0f, new Item.Properties().stacksTo(1))
    );

    // =========================================================================
    // ATEŞ ASASI — hasar: 5, sağ tık: 4 blok AoE ateş, 8 sn cooldown
    // =========================================================================

    /** Ateş Asası — hasar: 5, sağ tıkta etraftaki entity'leri tutuşturur. */
    public static final RegistryObject<Item> ATES_ASASI = ITEMS.register(
        "ates_asasi",
        () -> new AtesAsasiItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE))
    );

    // =========================================================================
    // Spawn Egg'ler
    // =========================================================================

    /** Rahim Koç spawn egg'i — koyu kahve + altın sarısı. */
    public static final RegistryObject<Item> RAHIM_KOC_SPAWN_EGG = ITEMS.register(
        "rahim_koc_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.RAHIM_KOC, 0x4a3000, 0xffd700,
            new Item.Properties())
    );

    /** Villa Hakan spawn egg'i — antrasit + turuncu. */
    public static final RegistryObject<Item> VILLA_HAKAN_SPAWN_EGG = ITEMS.register(
        "villa_hakan_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.VILLA_HAKAN, 0x1a1a1a, 0xff4500,
            new Item.Properties())
    );

    /** Hırt spawn egg'i — koyu gri + kırmızı. */
    public static final RegistryObject<Item> HIRT_SPAWN_EGG = ITEMS.register(
        "hirt_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.HIRT, 0x3a3a3a, 0xcc2222,
            new Item.Properties())
    );

    /** Recep İvedi spawn egg'i — toprak kahvesi + sarı. */
    public static final RegistryObject<Item> RECEP_IVEDI_SPAWN_EGG = ITEMS.register(
        "recep_ivedi_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.RECEP_IVEDI, 0x6b4423, 0xf0c040,
            new Item.Properties())
    );

    /** Puro — kozmetik consume item, yenince duman partikülü çıkar. */
    public static final RegistryObject<Item> PURO = ITEMS.register(
        "puro",
        () -> new PuroItem(new Item.Properties().stacksTo(16))
    );

    /** Fent — consume edilince Poison II + Nausea II (20 sn). */
    public static final RegistryObject<Item> FENT = ITEMS.register(
        "fent",
        () -> new FentItem(new Item.Properties().stacksTo(16))
    );

    /** Mutlak Butlan — elde tutulunca Resistance II verir, tüketilmez. */
    public static final RegistryObject<Item> MUTLAK_BUTLAN = ITEMS.register(
        "mutlak_butlan",
        () -> new MutlakButlanItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );

    /** Kemal Darkılıçoğlu spawn egg'i — koyu gri + beyaz. */
    public static final RegistryObject<Item> KEMAL_DARKILICOGLU_SPAWN_EGG = ITEMS.register(
        "kemal_darkilicoglu_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.KEMAL_DARKILICOGLU, 0x2d2d2d, 0xffffff,
            new Item.Properties())
    );

    /** George Floid spawn egg'i — gri + beyaz. */
    public static final RegistryObject<Item> GEORGE_FLOID_SPAWN_EGG = ITEMS.register(
        "george_floid_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.GEORGE_FLOID, 0x5a5a5a, 0xe0e0e0,
            new Item.Properties())
    );

    /** Abugat spawn egg'i — gri + sarı. */
    public static final RegistryObject<Item> ABUGAT_SPAWN_EGG = ITEMS.register(
        "abugat_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.ABUGAT, 0x5a5a5a, 0xffd700,
            new Item.Properties())
    );

    /** Fenerbahçe holigan spawn egg'i — lacivert + sarı. */
    public static final RegistryObject<Item> HOLIGAN_FENERBAHCE_SPAWN_EGG = ITEMS.register(
        "holigan_fenerbahce_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.HOLIGAN_FENERBAHCE, 0x003399, 0xFFD700,
            new Item.Properties())
    );

    /** Galatasaray holigan spawn egg'i — kırmızı + sarı. */
    public static final RegistryObject<Item> HOLIGAN_GALATASARAY_SPAWN_EGG = ITEMS.register(
        "holigan_galatasaray_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.HOLIGAN_GALATASARAY, 0xCC0000, 0xFFD700,
            new Item.Properties())
    );

    /** Beşiktaş holigan spawn egg'i — siyah + beyaz. */
    public static final RegistryObject<Item> HOLIGAN_BESIKTAS_SPAWN_EGG = ITEMS.register(
        "holigan_besiktas_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.HOLIGAN_BESIKTAS, 0x111111, 0xFFFFFF,
            new Item.Properties())
    );

    /** Trabzonspor holigan spawn egg'i — bordo + mavi. */
    public static final RegistryObject<Item> HOLIGAN_TRABZONSPOR_SPAWN_EGG = ITEMS.register(
        "holigan_trabzonspor_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.HOLIGAN_TRABZONSPOR, 0x7B0000, 0x0033AA,
            new Item.Properties())
    );

    /** Türk Polisi spawn egg'i — lacivert + kırmızı. */
    public static final RegistryObject<Item> TURK_POLISI_SPAWN_EGG = ITEMS.register(
        "turk_polisi_spawn_egg",
        () -> new ForgeSpawnEggItem(BixisEntities.TURK_POLISI, 0x003366, 0xCC0000,
            new Item.Properties())
    );

    private BixisItems() {}
}
