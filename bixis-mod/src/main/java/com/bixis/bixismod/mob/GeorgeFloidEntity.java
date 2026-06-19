package com.bixis.bixismod.mob;

import com.bixis.bixismod.effect.BixisEffects;
import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;

/**
 * George Floid — pasif mob, spawn'da infinite Kanama efekti alır.
 * Spawn'dan 60 tick sonra sunucu chat'e "<GeorgeFloid> nefes alamıyom" yazar.
 * Tüm sesler null.
 */
public class GeorgeFloidEntity extends PathfinderMob {

    private int spawnTick = 0;
    private boolean chatSent = false;

    public GeorgeFloidEntity(EntityType<? extends GeorgeFloidEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    /** @return attribute map */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData, tag);
        this.addEffect(new MobEffectInstance(BixisEffects.KANAMA.get(), Integer.MAX_VALUE, 0, false, false, false));
        return data;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (!chatSent) {
            spawnTick++;
            if (spawnTick >= 60) {
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.players().forEach(p ->
                        p.sendSystemMessage(Component.literal("<GeorgeFloid> nefes alamıyom")));
                }
                chatSent = true;
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        ItemEntity drop = new ItemEntity(level(),
            this.getX(), this.getY() + 0.5, this.getZ(),
            new ItemStack(BixisItems.FENT.get(), 1));
        drop.setDefaultPickUpDelay();
        level().addFreshEntity(drop);
    }

    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return BixisSounds.GEORGE_FLOID_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return null; }
}
