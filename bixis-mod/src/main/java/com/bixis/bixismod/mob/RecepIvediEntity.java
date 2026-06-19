package com.bixis.bixismod.mob;

import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

/**
 * Recep İvedi — nötr mob, çevresindeki blokları rastgele kırar.
 * Her 40-80 tickte bir 3 blok yarıçapında rastgele bir blok kırar.
 * Bedrock, obsidian, chest, spawner ve yapısal blokları kırmaz.
 * Idle ses: recep_idle (60-120 tick). Hurt/death: ses yok.
 */
public class RecepIvediEntity extends PathfinderMob {

    /** Kırılmayan bloklar listesi. */
    private static final Set<Block> PROTECTED_BLOCKS = Set.of(
        Blocks.BEDROCK,
        Blocks.OBSIDIAN,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.SPAWNER,
        Blocks.ENDER_CHEST,
        Blocks.COMMAND_BLOCK,
        Blocks.CHAIN_COMMAND_BLOCK,
        Blocks.REPEATING_COMMAND_BLOCK,
        Blocks.BARRIER,
        Blocks.END_PORTAL_FRAME,
        Blocks.END_PORTAL,
        Blocks.END_GATEWAY,
        Blocks.STRUCTURE_BLOCK,
        Blocks.STRUCTURE_VOID
    );

    private int blockBreakCooldown = 20;
    private int punchCooldown = 80;

    public RecepIvediEntity(EntityType<? extends RecepIvediEntity> type, Level level) {
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
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 8.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;

        if (blockBreakCooldown > 0) {
            blockBreakCooldown--;
        } else {
            breakRandomBlock();
            blockBreakCooldown = 40 + this.random.nextInt(41);
        }

        if (punchCooldown > 0) {
            punchCooldown--;
        } else {
            punchRandomEntity();
            punchCooldown = 160;
        }
    }

    private void punchRandomEntity() {
        AABB area = getBoundingBox().inflate(8.0);
        java.util.List<LivingEntity> targets = level().getEntitiesOfClass(
            LivingEntity.class, area, e -> e != this && e.isAlive());
        if (targets.isEmpty()) return;
        LivingEntity target = targets.get(this.random.nextInt(targets.size()));
        target.hurt(damageSources().mobAttack(this), 3.0f);
        level().playSound(null, blockPosition(),
            BixisSounds.RECEP_IDLE.get(), SoundSource.HOSTILE, 1.0f, 1.2f);
    }

    private void breakRandomBlock() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        BlockPos center = blockPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = this.random.nextInt(7) - 3;
            int dy = this.random.nextInt(7) - 3;
            int dz = this.random.nextInt(7) - 3;
            BlockPos pos = center.offset(dx, dy, dz);
            BlockState state = serverLevel.getBlockState(pos);
            if (!state.isAir() && !PROTECTED_BLOCKS.contains(state.getBlock())
                    && state.getDestroySpeed(serverLevel, pos) >= 0) {
                serverLevel.destroyBlock(pos, true, this);
                return;
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() { return BixisSounds.RECEP_IDLE.get(); }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() { return null; }

    /** Idle ses aralığı: 60-120 tick. */
    @Override
    public int getAmbientSoundInterval() { return 60 + this.random.nextInt(61); }
}
