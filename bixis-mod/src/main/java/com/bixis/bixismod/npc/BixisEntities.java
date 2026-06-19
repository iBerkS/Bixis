package com.bixis.bixismod.npc;

import com.bixis.bixismod.BixisMod;
import com.bixis.bixismod.Constants;
import com.bixis.bixismod.mob.AbugatEntity;
import com.bixis.bixismod.mob.BesiktasHoliganEntity;
import com.bixis.bixismod.mob.FenerbahceHoliganEntity;
import com.bixis.bixismod.mob.GalatasarayHoliganEntity;
import com.bixis.bixismod.mob.GeorgeFloidEntity;
import com.bixis.bixismod.mob.HirtEntity;
import com.bixis.bixismod.mob.KemalDarkilicogluEntity;
import com.bixis.bixismod.mob.RecepIvediEntity;
import com.bixis.bixismod.mob.TrabzonsporHoliganEntity;
import com.bixis.bixismod.mob.TurkPolisiEntity;
import com.bixis.bixismod.weapon.BulletProjectileEntity;
import com.bixis.bixismod.weapon.MizrakProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Tüm bixis entity type kayıtlarını tutan sınıf. */
public final class BixisEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BixisMod.MOD_ID);

    /** Döviz bürosu NPC'si. */
    public static final RegistryObject<EntityType<RahimKocEntity>> RAHIM_KOC =
        ENTITY_TYPES.register("rahim_koc", () ->
            EntityType.Builder.<RahimKocEntity>of(RahimKocEntity::new, MobCategory.MISC)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(10)
                .build("rahim_koc")
        );

    /** Silah dükkanı NPC'si. */
    public static final RegistryObject<EntityType<VillaHakanEntity>> VILLA_HAKAN =
        ENTITY_TYPES.register("villa_hakan", () ->
            EntityType.Builder.<VillaHakanEntity>of(VillaHakanEntity::new, MobCategory.MISC)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(10)
                .build("villa_hakan")
        );

    /** Desert Eagle mermisi projectile entity'si. */
    public static final RegistryObject<EntityType<BulletProjectileEntity>> BULLET =
        ENTITY_TYPES.register("bullet", () ->
            EntityType.Builder.<BulletProjectileEntity>of(BulletProjectileEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(4)
                .updateInterval(5)
                .build("bullet")
        );

    /** Saldırgan mob — yerden item alır, %10 iron hancer ile doğar. */
    public static final RegistryObject<EntityType<HirtEntity>> HIRT =
        ENTITY_TYPES.register("hirt", () ->
            EntityType.Builder.<HirtEntity>of(HirtEntity::new, MobCategory.MONSTER)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("hirt")
        );

    /** Nötr mob — çevresindeki blokları rastgele kırar. */
    public static final RegistryObject<EntityType<RecepIvediEntity>> RECEP_IVEDI =
        ENTITY_TYPES.register("recep_ivedi", () ->
            EntityType.Builder.<RecepIvediEntity>of(RecepIvediEntity::new, MobCategory.CREATURE)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("recep_ivedi")
        );

    /** Pasif mob — infinite Kanama efekti, spawn'dan 60 tick sonra chat mesajı. */
    public static final RegistryObject<EntityType<GeorgeFloidEntity>> GEORGE_FLOID =
        ENTITY_TYPES.register("george_floid", () ->
            EntityType.Builder.<GeorgeFloidEntity>of(GeorgeFloidEntity::new, MobCategory.CREATURE)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("george_floid")
        );

    /** Tamamen pasif, sandalyede oturan mob. */
    public static final RegistryObject<EntityType<KemalDarkilicogluEntity>> KEMAL_DARKILICOGLU =
        ENTITY_TYPES.register("kemal_darkilicoglu", () ->
            EntityType.Builder.<KemalDarkilicogluEntity>of(KemalDarkilicogluEntity::new, MobCategory.CREATURE)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("kemal_darkilicoglu")
        );

    /** Pasif dans eden mob — yerinde döner, zıplar, iki farklı ses. */
    public static final RegistryObject<EntityType<AbugatEntity>> ABUGAT =
        ENTITY_TYPES.register("abugat", () ->
            EntityType.Builder.<AbugatEntity>of(AbugatEntity::new, MobCategory.CREATURE)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("abugat")
        );

    /** Fenerbahçe takımı holigan. */
    public static final RegistryObject<EntityType<FenerbahceHoliganEntity>> HOLIGAN_FENERBAHCE =
        ENTITY_TYPES.register("holigan_fenerbahce", () ->
            EntityType.Builder.<FenerbahceHoliganEntity>of(FenerbahceHoliganEntity::new, MobCategory.MONSTER)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("holigan_fenerbahce")
        );

    /** Galatasaray takımı holigan. */
    public static final RegistryObject<EntityType<GalatasarayHoliganEntity>> HOLIGAN_GALATASARAY =
        ENTITY_TYPES.register("holigan_galatasaray", () ->
            EntityType.Builder.<GalatasarayHoliganEntity>of(GalatasarayHoliganEntity::new, MobCategory.MONSTER)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("holigan_galatasaray")
        );

    /** Beşiktaş takımı holigan. */
    public static final RegistryObject<EntityType<BesiktasHoliganEntity>> HOLIGAN_BESIKTAS =
        ENTITY_TYPES.register("holigan_besiktas", () ->
            EntityType.Builder.<BesiktasHoliganEntity>of(BesiktasHoliganEntity::new, MobCategory.MONSTER)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("holigan_besiktas")
        );

    /** Trabzonspor takımı holigan. */
    public static final RegistryObject<EntityType<TrabzonsporHoliganEntity>> HOLIGAN_TRABZONSPOR =
        ENTITY_TYPES.register("holigan_trabzonspor", () ->
            EntityType.Builder.<TrabzonsporHoliganEntity>of(TrabzonsporHoliganEntity::new, MobCategory.MONSTER)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("holigan_trabzonspor")
        );

    /** Hostile mob — rüşvet mekanizması ile 3000 tick passive olur. */
    public static final RegistryObject<EntityType<TurkPolisiEntity>> TURK_POLISI =
        ENTITY_TYPES.register("turk_polisi", () ->
            EntityType.Builder.<TurkPolisiEntity>of(TurkPolisiEntity::new, MobCategory.MONSTER)
                .sized(Constants.NPC_WIDTH, Constants.NPC_HEIGHT)
                .clientTrackingRange(8)
                .build("turk_polisi")
        );

    /** Fırlatılmış mızrak projectile entity'si. */
    public static final RegistryObject<EntityType<MizrakProjectileEntity>> MIZRAK_PROJECTILE =
        ENTITY_TYPES.register("mizrak_projectile", () ->
            EntityType.Builder.<MizrakProjectileEntity>of(MizrakProjectileEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .clientTrackingRange(4)
                .updateInterval(20)
                .build("mizrak_projectile")
        );

    /**
     * ENTITY_TYPES register'ını mod event bus'a bağlar.
     *
     * @param modBus mod yükleme event bus'ı
     */
    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }

    private BixisEntities() {}
}
