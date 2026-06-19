package com.bixis.bixismod.mob;

import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;

/**
 * Hırt — saldırgan, yerden item alan mob.
 * 4 texture varyantı (HirtVariant 0-3) — spawn'da rastgele seçilir,
 * NBT'de saklanır, EntityData ile client/server senkronize edilir.
 */
public class HirtEntity extends Monster {

    /** Texture varyantı (0-3) — synced EntityData. */
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
        SynchedEntityData.defineId(HirtEntity.class, EntityDataSerializers.INT);

    /** NBT anahtarı — varyant index. */
    public static final String TAG_VARIANT = "HirtVariant";

    public HirtEntity(EntityType<? extends HirtEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    // -------------------------------------------------------------------------
    // Varyant senkronizasyonu

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, 0);
    }

    /** @return texture varyant index (0-3) */
    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    private void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    // -------------------------------------------------------------------------
    // Spawn — rastgele varyant ata

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData, tag);
        this.setVariant(this.random.nextInt(4));
        return data;
    }

    // -------------------------------------------------------------------------
    // NBT — varyantı kaydet/yükle

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_VARIANT, this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(tag.getInt(TAG_VARIANT));
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
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** @return attribute map */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    // -------------------------------------------------------------------------
    // Item pickup

    @Override
    public boolean canPickUpLoot() { return true; }

    /**
     * MAINHAND: 2.0f → %100 drop (spawn ekipmanı + yerden alınan).
     * Diğer slotlar: vanilla varsayılanı (~0.085f).
     */
    @Override
    protected float getEquipmentDropChance(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? 2.0f : super.getEquipmentDropChance(slot);
    }

    /** %25 şansla iron hancer ile doğar; silahsız spawna dokunulmaz. */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        if (random.nextFloat() < 0.25f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BixisItems.IRON_HANCER.get()));
        }
    }

    // -------------------------------------------------------------------------
    // Saldırı ve sesler

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            this.level().playSound(null, this.blockPosition(),
                BixisSounds.HIRT_AGGRO.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() { return BixisSounds.HIRT_IDLE.get(); }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.VINDICATOR_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VINDICATOR_DEATH; }

    @Override
    public int getAmbientSoundInterval() { return 80 + this.random.nextInt(81); }
}
