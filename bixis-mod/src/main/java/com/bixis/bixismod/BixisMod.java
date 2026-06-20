package com.bixis.bixismod;

import com.bixis.bixismod.command.BixisCommand;
import com.bixis.bixismod.config.BixisArenaSpawnsConfig;
import com.bixis.bixismod.config.BixisCheckpointsConfig;
import com.bixis.bixismod.config.BixisRaceSpawnsConfig;
import com.bixis.bixismod.config.BixisRatesConfig;
import com.bixis.bixismod.effect.BixisEffects;
import com.bixis.bixismod.item.BixisCreativeTabs;
import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.item.FenerbahceFormaEventHandler;
import com.bixis.bixismod.item.FaizKalkaniEventHandler;
import com.bixis.bixismod.item.MutlakButlanEventHandler;
import com.bixis.bixismod.item.TcPasaportuEventHandler;
import com.bixis.bixismod.mob.AbugatEntity;
import com.bixis.bixismod.mob.BesiktasHoliganEntity;
import com.bixis.bixismod.mob.FenerbahceHoliganEntity;
import com.bixis.bixismod.mob.GalatasarayHoliganEntity;
import com.bixis.bixismod.mob.GeorgeFloidEntity;
import com.bixis.bixismod.mob.HirtEntity;
import com.bixis.bixismod.mob.HoliganEntity;
import com.bixis.bixismod.mob.KemalDarkilicogluEntity;
import com.bixis.bixismod.mob.RecepIvediEntity;
import com.bixis.bixismod.mob.TrabzonsporHoliganEntity;
import com.bixis.bixismod.mob.TurkPolisiEntity;
import com.bixis.bixismod.npc.BixisEntities;
import com.bixis.bixismod.npc.RahimKocEntity;
import com.bixis.bixismod.npc.VillaHakanEntity;
import com.bixis.bixismod.weapon.BixisSounds;
import com.bixis.bixismod.weapon.GunEventHandler;
import com.bixis.bixismod.weapon.WeaponEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Ana mod entry point. Mod ID: {@value MOD_ID} */
@Mod(BixisMod.MOD_ID)
public class BixisMod {

    public static final String MOD_ID = "bixis";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public BixisMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        BixisItems.ITEMS.register(modBus);
        BixisCreativeTabs.register(modBus);
        BixisEntities.register(modBus);
        BixisEffects.register(modBus);
        BixisSounds.register(modBus);

        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onEntityAttributes);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.register(new TcPasaportuEventHandler());
        MinecraftForge.EVENT_BUS.register(new MutlakButlanEventHandler());
        MinecraftForge.EVENT_BUS.register(new FaizKalkaniEventHandler());
        MinecraftForge.EVENT_BUS.register(new FenerbahceFormaEventHandler());
        MinecraftForge.EVENT_BUS.register(new WeaponEventHandler());
        MinecraftForge.EVENT_BUS.register(GunEventHandler.INSTANCE);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BixisRatesConfig.load(FMLPaths.CONFIGDIR.get());
            BixisRaceSpawnsConfig.init(FMLPaths.CONFIGDIR.get());
            BixisArenaSpawnsConfig.init(FMLPaths.CONFIGDIR.get());
            BixisCheckpointsConfig.init(FMLPaths.CONFIGDIR.get());
        });
    }

    /**
     * Entity attribute'larını Forge'a kaydeder.
     * Her entity type'ın kendi createAttributes() metodu kullanılır.
     */
    private void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(BixisEntities.RAHIM_KOC.get(),    RahimKocEntity.createAttributes().build());
        event.put(BixisEntities.VILLA_HAKAN.get(), VillaHakanEntity.createAttributes().build());
        event.put(BixisEntities.HIRT.get(),         HirtEntity.createAttributes().build());
        event.put(BixisEntities.RECEP_IVEDI.get(),  RecepIvediEntity.createAttributes().build());
        event.put(BixisEntities.GEORGE_FLOID.get(),        GeorgeFloidEntity.createAttributes().build());
        event.put(BixisEntities.ABUGAT.get(),              AbugatEntity.createAttributes().build());
        event.put(BixisEntities.KEMAL_DARKILICOGLU.get(), KemalDarkilicogluEntity.createAttributes().build());
        event.put(BixisEntities.HOLIGAN_FENERBAHCE.get(),  HoliganEntity.createAttributes().build());
        event.put(BixisEntities.HOLIGAN_GALATASARAY.get(), HoliganEntity.createAttributes().build());
        event.put(BixisEntities.HOLIGAN_BESIKTAS.get(),    HoliganEntity.createAttributes().build());
        event.put(BixisEntities.HOLIGAN_TRABZONSPOR.get(), HoliganEntity.createAttributes().build());
        event.put(BixisEntities.TURK_POLISI.get(),         TurkPolisiEntity.createAttributes().build());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        BixisCommand.register(event.getDispatcher());
    }
}
