package com.bixis.bixismod.item;

import com.bixis.bixismod.BixisMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Bixis Mod creative sekmesini kaydeden sınıf. */
public final class BixisCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BixisMod.MOD_ID);

    /** Tüm bixis item'larını barındıran tek sekme. İkonu: turk_lirasi. */
    public static final RegistryObject<CreativeModeTab> BIXIS_TAB = CREATIVE_MODE_TABS.register(
        "bixis_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.bixis"))
            .icon(() -> new ItemStack(BixisItems.TURK_LIRASI.get()))
            .displayItems((params, output) -> {
                // Para birimi
                output.accept(BixisItems.TURK_LIRASI.get());
                // Mağaza & ekipman
                output.accept(BixisItems.FENERBAHCE_KILICI.get());
                output.accept(BixisItems.FENERBAHCE_FORMA.get());
                output.accept(BixisItems.M4.get());
                output.accept(BixisItems.AWP.get());
                output.accept(BixisItems.DESERT_EAGLE.get());
                output.accept(BixisItems.MERMI.get());
                output.accept(BixisItems.TC_PASAPORTU.get());
                // Hançer
                output.accept(BixisItems.IRON_HANCER.get());
                output.accept(BixisItems.DIAMOND_HANCER.get());
                output.accept(BixisItems.NETHERITE_HANCER.get());
                // Mızrak
                output.accept(BixisItems.IRON_MIZRAK.get());
                output.accept(BixisItems.DIAMOND_MIZRAK.get());
                output.accept(BixisItems.NETHERITE_MIZRAK.get());
                // Yatağan
                output.accept(BixisItems.IRON_YATAGAN.get());
                output.accept(BixisItems.DIAMOND_YATAGAN.get());
                output.accept(BixisItems.NETHERITE_YATAGAN.get());
                // Gaddare
                output.accept(BixisItems.IRON_GADDARE.get());
                output.accept(BixisItems.DIAMOND_GADDARE.get());
                output.accept(BixisItems.NETHERITE_GADDARE.get());
                // Ateş Asası
                output.accept(BixisItems.ATES_ASASI.get());
                // Özel item'lar
                output.accept(BixisItems.FAIZ_KALKANI.get());
                output.accept(BixisItems.PURO.get());
                output.accept(BixisItems.FENT.get());
                output.accept(BixisItems.MUTLAK_BUTLAN.get());
                // Spawn egg'ler
                output.accept(BixisItems.RAHIM_KOC_SPAWN_EGG.get());
                output.accept(BixisItems.VILLA_HAKAN_SPAWN_EGG.get());
                output.accept(BixisItems.HIRT_SPAWN_EGG.get());
                output.accept(BixisItems.RECEP_IVEDI_SPAWN_EGG.get());
                output.accept(BixisItems.KEMAL_DARKILICOGLU_SPAWN_EGG.get());
                output.accept(BixisItems.GEORGE_FLOID_SPAWN_EGG.get());
                output.accept(BixisItems.ABUGAT_SPAWN_EGG.get());
                output.accept(BixisItems.HOLIGAN_FENERBAHCE_SPAWN_EGG.get());
                output.accept(BixisItems.HOLIGAN_GALATASARAY_SPAWN_EGG.get());
                output.accept(BixisItems.HOLIGAN_BESIKTAS_SPAWN_EGG.get());
                output.accept(BixisItems.HOLIGAN_TRABZONSPOR_SPAWN_EGG.get());
                output.accept(BixisItems.TURK_POLISI_SPAWN_EGG.get());
            })
            .build()
    );

    /**
     * CREATIVE_MODE_TABS register'ını mod event bus'a bağlar.
     *
     * @param modBus mod yükleme event bus'ı
     */
    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }

    private BixisCreativeTabs() {}
}
