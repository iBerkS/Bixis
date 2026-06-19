package com.bixis.bixismod.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Kemal Darkılıçoğlu — tamamen pasif, sandalyede oturan mob.
 * Spawn'da stairs koyar, ArmorStand'a anında biner (gravity kapalı).
 * Ölünce ArmorStand temizlenir.
 */
public class KemalDarkilicogluEntity extends PathfinderMob {

    @Nullable
    private UUID chairUUID;

    public KemalDarkilicogluEntity(EntityType<? extends KemalDarkilicogluEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void registerGoals() {}

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, net.minecraft.world.DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(levelAccessor, difficulty, reason, groupData, tag);

        if (levelAccessor instanceof ServerLevel serverLevel) {
            BlockPos pos = this.blockPosition();

            // 1. Stairs
            BlockState stairState = Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.fromYRot(this.getYRot()).getOpposite())
                .setValue(StairBlock.HALF, Half.BOTTOM);
            serverLevel.setBlockAndUpdate(pos, stairState);

            // 2. ArmorStand — gravity kapalı, sabit konum
            ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, serverLevel);
            stand.moveTo(pos.getX() + 0.5, pos.getY() - 0.9, pos.getZ() + 0.5, this.getYRot(), 0.0f);
            stand.setInvisible(true);
            stand.setNoGravity(true);
            stand.setInvulnerable(true);
            serverLevel.addFreshEntity(stand);

            this.chairUUID = stand.getUUID();

            // 3. Anında bindir
            this.startRiding(stand, true);

            // 4. Kemal de havada sabit kalsın
            this.setNoGravity(true);
        }

        return data;
    }

    @Override
    public void die(DamageSource source) {
        this.stopRiding();
        if (!level().isClientSide && chairUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity stand = serverLevel.getEntity(chairUUID);
            if (stand != null) stand.discard();
        }
        super.die(source);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (chairUUID != null) tag.putUUID("ChairUUID", chairUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("ChairUUID")) chairUUID = tag.getUUID("ChairUUID");
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
            level(), getX(), getY() + 0.5, getZ(),
            new net.minecraft.world.item.ItemStack(com.bixis.bixismod.item.BixisItems.MUTLAK_BUTLAN.get(), 1));
        drop.setDefaultPickUpDelay();
        level().addFreshEntity(drop);
    }

    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override protected SoundEvent getDeathSound() { return null; }
}
