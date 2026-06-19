package com.bixis.bixismod.weapon;

import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.npc.BixisEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Desert Eagle mermisi — ThrowableItemProjectile tabanlı, yüksek hızlı düz yörüngeli.
 * Herhangi bir çarpışmada kaybolur. Görsel: bixis:mermi item modeli.
 */
public class BulletProjectileEntity extends ThrowableItemProjectile {

    private float damage = 8.0f;

    /** Ağ deserialization constructor. */
    public BulletProjectileEntity(EntityType<BulletProjectileEntity> type, Level level) {
        super(type, level);
        this.setItem(new ItemStack(BixisItems.MERMI.get()));
    }

    /**
     * Ateşleme constructor.
     *
     * @param level   dünya
     * @param shooter fırlatan entity
     * @param damage  verilen hasar
     */
    public BulletProjectileEntity(Level level, LivingEntity shooter, float damage) {
        super(BixisEntities.BULLET.get(), shooter, level);
        this.setItem(new ItemStack(BixisItems.MERMI.get()));
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return BixisItems.MERMI.get();
    }

    /** 0.02f — mermiler neredeyse düz gider. */
    @Override
    protected float getGravity() {
        return 0.02f;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner  = this.getOwner();
        target.hurt(this.damageSources().thrown(this, owner != null ? owner : this), damage);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("BulletDamage", damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("BulletDamage");
    }
}
