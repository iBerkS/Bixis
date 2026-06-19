package com.bixis.bixismod.mob;

import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Abugat — pasif dans eden mob.
 * Her tick yRot += 8f (yerinde döner).
 * Her 10 tick'te bir 0.15 yukarı itilir (zıplama hissi).
 * Idle sesi: ~2sn (40 tick bekleme), Sing sesi: ~4sn (80 tick bekleme).
 * Çakışmayı önlemek için isPlayingSound flag'i kullanılır.
 * Drop: 1x desert_eagle, 3-5x mermi, 10-20x turk_lirasi.
 */
public class AbugatEntity extends PathfinderMob {

    private boolean isPlayingSound = false;
    private int soundCooldown = 60;

    public AbugatEntity(EntityType<? extends AbugatEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    /** @return attribute map */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    public void tick() {
        super.tick();

        // Dans — her tick döndür
        this.setYRot(this.getYRot() + 8.0f);
        this.yRotO = this.getYRot();

        // Zıplama hissi — her 10 tick
        if (this.tickCount % 10 == 0) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, 0.15, motion.z);
        }

        if (level().isClientSide) return;

        if (isPlayingSound) {
            if (--soundCooldown <= 0) {
                isPlayingSound = false;
                // Bir sonraki ses için kısa bekleme
                soundCooldown = 20 + this.random.nextInt(20);
            }
            return;
        }

        if (--soundCooldown <= 0) {
            // %40 idle, %20 sing, %40 sessiz bekleme
            int roll = this.random.nextInt(10);
            if (roll < 4) {
                level().playSound(null, blockPosition(),
                    BixisSounds.ABUGAT_IDLE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                isPlayingSound = true;
                soundCooldown = 40; // ~2sn ses süresi bekleme
            } else if (roll < 6) {
                level().playSound(null, blockPosition(),
                    BixisSounds.ABUGAT_SING.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                isPlayingSound = true;
                soundCooldown = 80; // ~4sn ses süresi bekleme
            } else {
                soundCooldown = 40 + this.random.nextInt(41); // sessiz bekleme
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (!(level() instanceof ServerLevel)) return;

        spawnDrop(new ItemStack(BixisItems.DESERT_EAGLE.get(), 1));
        spawnDrop(new ItemStack(BixisItems.MERMI.get(), 3 + this.random.nextInt(3)));
        spawnDrop(new ItemStack(BixisItems.TURK_LIRASI.get(), 10 + this.random.nextInt(11)));
    }

    private void spawnDrop(ItemStack stack) {
        ItemEntity entity = new ItemEntity(level(),
            this.getX(), this.getY() + 0.5, this.getZ(), stack);
        entity.setDefaultPickUpDelay();
        level().addFreshEntity(entity);
    }

    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override protected SoundEvent getDeathSound() { return null; }
}
