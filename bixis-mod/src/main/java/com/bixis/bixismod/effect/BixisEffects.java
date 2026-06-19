package com.bixis.bixismod.effect;

import com.bixis.bixismod.BixisMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Tüm bixis MobEffect kayıtları. */
public final class BixisEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, BixisMod.MOD_ID);

    /** Kanama — sağlığı sıfıra kadar götürebilen zararlı efekt. */
    public static final RegistryObject<MobEffect> KANAMA = EFFECTS.register("kanama", KanamaEffect::new);

    /**
     * EFFECTS register'ını mod event bus'a bağlar.
     *
     * @param modBus mod yükleme event bus'ı
     */
    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }

    private BixisEffects() {}
}
