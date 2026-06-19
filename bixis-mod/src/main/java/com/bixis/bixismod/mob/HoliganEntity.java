package com.bixis.bixismod.mob;

import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MobSpawnType;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * Holigan mob'ların base sınıfı.
 * Alt sınıflar {@link #getTeam()} ve {@link #getSpawnSound()} metodlarını override eder.
 *
 * <p>Saldırı mekaniği:
 * <ul>
 *   <li>Oyuncu vurursa: 32 blok yarıçapındaki aynı takım holiganları da o oyuncuyu hedef alır.</li>
 *   <li>Her 40 tick'te: 16 blok içinde farklı takım holigan varsa onu hedef alır, oyuncuyu unutur.</li>
 * </ul>
 */
public abstract class HoliganEntity extends Monster {

    /** Rakip holigan kontrol sayacı. */
    private int rivalCheckTimer = 0;

    public enum Team { FENERBAHCE, GALATASARAY, BESIKTAS, TRABZONSPOR }

    protected HoliganEntity(EntityType<? extends HoliganEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    /** @return bu holiganın takımı */
    public abstract Team getHoliganTeam();

    // -------------------------------------------------------------------------
    // Attribute

    /** @return holigan attribute builder */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    // -------------------------------------------------------------------------
    // AI hedefleri

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // -------------------------------------------------------------------------
    // Saldırı — vurunca aggro ses

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            this.level().playSound(null, this.blockPosition(),
                BixisSounds.HOLIGAN_AGGRO.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
        }
        return hit;
    }

    // -------------------------------------------------------------------------
    // Oyuncu vurursa — 32 blok içindeki aynı takım holiganları da aggro olur

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && source.getEntity() instanceof Player player && !this.level().isClientSide) {
            List<HoliganEntity> teammates = this.level().getEntitiesOfClass(
                HoliganEntity.class,
                this.getBoundingBox().inflate(32),
                h -> h != this && h.getHoliganTeam() == this.getHoliganTeam()
            );
            for (HoliganEntity teammate : teammates) {
                teammate.setTarget(player);
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // aiStep — her 40 tick'te rakip holigan ara

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        // Rakip hedef kontrol
        if (++rivalCheckTimer >= 40) {
            rivalCheckTimer = 0;
            List<HoliganEntity> rivals = this.level().getEntitiesOfClass(
                HoliganEntity.class,
                this.getBoundingBox().inflate(16),
                h -> h != this && h.getHoliganTeam() != this.getHoliganTeam()
            );
            if (!rivals.isEmpty()) {
                rivals.stream()
                    .min(Comparator.comparingDouble(h -> h.distanceToSqr(this)))
                    .ifPresent(this::setTarget);
            }
        }

        // Rakip hedef menzil dışına çıktıysa temizle
        LivingEntity currentTarget = this.getTarget();
        if (currentTarget instanceof HoliganEntity rival
                && rival.getHoliganTeam() != this.getHoliganTeam()
                && this.distanceToSqr(rival) > 16.0 * 16.0) {
            this.setTarget(null);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag) {
        return super.finalizeSpawn(level, difficulty, reason, groupData, tag);
    }

    // -------------------------------------------------------------------------
    // Sesler — ambient 60 saniyede bir; hurt/death yok

    @Override @Nullable protected abstract SoundEvent getAmbientSound();

    @Override
    public int getAmbientSoundInterval() { return 1200; }

    @Override @Nullable protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override @Nullable protected SoundEvent getDeathSound() { return null; }
}
