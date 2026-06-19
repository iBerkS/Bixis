package com.bixis.bixismod.weapon;

import com.bixis.bixismod.BixisMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Bixis mod ses event kayıtları — Desert Eagle, AWP ve M4 sesleri. */
public final class BixisSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BixisMod.MOD_ID);

    // -------------------------------------------------------------------------
    // NPC trade sesleri

    /** Villa Hakan trade sesi — villa_hakan_1.ogg */
    public static final RegistryObject<SoundEvent> VILLA_HAKAN_TRADE = register("villa_hakan_trade");
    /** Rahim Koç trade sesi — rahim_koc_1.ogg */
    public static final RegistryObject<SoundEvent> RAHIM_KOC_TRADE   = register("rahim_koc_trade");

    // -------------------------------------------------------------------------
    // Hırt mob sesleri

    /** Ambient ses — hirt_idle_1.ogg / hirt_idle_2.ogg (sounds.json'da çoklu entry) */
    public static final RegistryObject<SoundEvent> HIRT_IDLE   = register("hirt_idle");
    /** Saldırı sesi — hirt_aggro_1.ogg / hirt_aggro_2.ogg */
    public static final RegistryObject<SoundEvent> HIRT_AGGRO  = register("hirt_aggro");

    // -------------------------------------------------------------------------
    // Recep İvedi mob sesleri

    /** Ambient ses — recep_ivedi_1.ogg */
    public static final RegistryObject<SoundEvent> RECEP_IDLE  = register("recep_idle");

    // -------------------------------------------------------------------------
    // George Floid mob sesleri

    /** Hasar sesi — george_floid_hurt.ogg */
    public static final RegistryObject<SoundEvent> GEORGE_FLOID_HURT = register("george_floid_hurt");

    // -------------------------------------------------------------------------
    // Holigan mob sesleri

    /** Saldırı sesi — holigan_aggro.ogg */
    public static final RegistryObject<SoundEvent> HOLIGAN_AGGRO                = register("holigan_aggro");
    /** Fenerbahçe holigan spawn sesi — holigan_fenerbahce_spawn.ogg */
    public static final RegistryObject<SoundEvent> HOLIGAN_FENERBAHCE_SPAWN     = register("holigan_fenerbahce_spawn");
    /** Galatasaray holigan spawn sesi — holigan_galatasaray_spawn.ogg */
    public static final RegistryObject<SoundEvent> HOLIGAN_GALATASARAY_SPAWN    = register("holigan_galatasaray_spawn");
    /** Beşiktaş holigan spawn sesi — holigan_besiktas_spawn.ogg */
    public static final RegistryObject<SoundEvent> HOLIGAN_BESIKTAS_SPAWN       = register("holigan_besiktas_spawn");

    // -------------------------------------------------------------------------
    // Abugat mob sesleri

    /** Ambient ses — abugat_idle_1.ogg / abugat_idle_2.ogg */
    public static final RegistryObject<SoundEvent> ABUGAT_IDLE = register("abugat_idle");
    /** Şarkı sesi — abugat_sing_1.ogg / abugat_sing_2.ogg */
    public static final RegistryObject<SoundEvent> ABUGAT_SING = register("abugat_sing");

    // -------------------------------------------------------------------------
    // Ateş Asası

    /** @see "assets/bixis/sounds/ates_asasi_cast.ogg" */
    public static final RegistryObject<SoundEvent> ATES_ASASI_CAST = register("ates_asasi_cast");

    // -------------------------------------------------------------------------
    // Desert Eagle

    /** @see "assets/bixis/sounds/desert_eagle_fire.ogg" */
    public static final RegistryObject<SoundEvent> DESERT_EAGLE_FIRE = register("desert_eagle_fire");
    /** @see "assets/bixis/sounds/desert_eagle_empty.ogg" */
    public static final RegistryObject<SoundEvent> DESERT_EAGLE_EMPTY = register("desert_eagle_empty");
    /** @see "assets/bixis/sounds/desert_eagle_equip.ogg" */
    public static final RegistryObject<SoundEvent> DESERT_EAGLE_EQUIP = register("desert_eagle_equip");
    /** @see "assets/bixis/sounds/desert_eagle_reload.ogg" */
    public static final RegistryObject<SoundEvent> DESERT_EAGLE_RELOAD = register("desert_eagle_reload");

    // -------------------------------------------------------------------------
    // AWP

    /** @see "assets/bixis/sounds/awp_fire.ogg" */
    public static final RegistryObject<SoundEvent> AWP_FIRE   = register("awp_fire");
    /** @see "assets/bixis/sounds/awp_empty.ogg" */
    public static final RegistryObject<SoundEvent> AWP_EMPTY  = register("awp_empty");
    /** @see "assets/bixis/sounds/awp_equip.ogg" */
    public static final RegistryObject<SoundEvent> AWP_EQUIP  = register("awp_equip");
    /** @see "assets/bixis/sounds/awp_reload.ogg" */
    public static final RegistryObject<SoundEvent> AWP_RELOAD = register("awp_reload");

    // -------------------------------------------------------------------------
    // M4

    /** @see "assets/bixis/sounds/m4_fire.ogg" */
    public static final RegistryObject<SoundEvent> M4_FIRE    = register("m4_fire");
    /** @see "assets/bixis/sounds/m4_empty.ogg" */
    public static final RegistryObject<SoundEvent> M4_EMPTY   = register("m4_empty");
    /** @see "assets/bixis/sounds/m4_equip.ogg" */
    public static final RegistryObject<SoundEvent> M4_EQUIP   = register("m4_equip");
    /** @see "assets/bixis/sounds/m4_reload.ogg" */
    public static final RegistryObject<SoundEvent> M4_RELOAD  = register("m4_reload");

    // -------------------------------------------------------------------------

    /**
     * SOUNDS register'ını mod event bus'a bağlar.
     *
     * @param modBus mod yükleme event bus'ı
     */
    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    @SuppressWarnings("removal")
    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name,
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BixisMod.MOD_ID, name)));
    }

    private BixisSounds() {}
}
