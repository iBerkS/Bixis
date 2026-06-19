package com.bixis.bixismod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Kanama efekti — her tick hasar verir, zehirden hızlı ve öldürebilir.
 * Vanilla Poison'dan farkı: sağlık %1'in altına inse de hasar verir.
 */
public class KanamaEffect extends MobEffect {

    public KanamaEffect() {
        super(MobEffectCategory.HARMFUL, 0xCC0000);
    }

    /**
     * Her aktif tick'te magic hasar uygular.
     * Amplifier 0 = Kanama I (1 hasar), 1 = Kanama II (2 hasar).
     */
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().magic(), 1.0f + amplifier);
    }

    /**
     * Zehirden hızlı: her 8 tick'te bir (Amplifier II = her 4 tick).
     */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = Math.max(4, 8 >> amplifier);
        return duration % interval == 0;
    }
}
