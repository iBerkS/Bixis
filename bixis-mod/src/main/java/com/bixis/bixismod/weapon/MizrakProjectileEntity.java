package com.bixis.bixismod.weapon;

import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.npc.BixisEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Fırlatılmış Mızrak — ThrowableItemProjectile tabanlı, trident mantığı:
 * <ul>
 *   <li>Minimal yerçekimi (0.05f), yüksek hızda düz gider</li>
 *   <li>Duvara/zemine çarpınca saplanır, kaybolmaz</li>
 *   <li>Entity'ye çarpınca hasar verir, yere düşer</li>
 *   <li>Sahip oyuncu yakınından geçince otomatik toplanır</li>
 *   <li>Saplanmış mızrağa sağ tık ile de toplanabilir</li>
 *   <li>Anti-dupe: {@code pickedUp} flag, iki yöntem aynı anda aktif olamaz</li>
 * </ul>
 * Render: {@code getItem()} sayesinde ThrownItemRenderer mızrak item modelini gösterir.
 */
public class MizrakProjectileEntity extends ThrowableItemProjectile {

    private boolean stuck     = false;
    private boolean pickedUp  = false;
    private double  thrownDamage = 5.0;

    // -------------------------------------------------------------------------
    // Constructor'lar

    /** Ağ deserialization constructor. */
    public MizrakProjectileEntity(EntityType<MizrakProjectileEntity> type, Level level) {
        super(type, level);
    }

    /**
     * Fırlatma constructor — item render için stack kaydedilir.
     *
     * @param level      dünya
     * @param shooter    fırlatan entity
     * @param stack      orijinal mızrak item stack'i (render + pickup için)
     * @param damage     fırlatma hasarı
     */
    public MizrakProjectileEntity(Level level, LivingEntity shooter,
                                   ItemStack stack, double damage) {
        super(BixisEntities.MIZRAK_PROJECTILE.get(), shooter, level);
        this.setItem(stack.copyWithCount(1));
        this.thrownDamage = damage;
    }

    // -------------------------------------------------------------------------
    // ThrowableItemProjectile API

    /** Network deserialization için default render item. */
    @Override
    protected Item getDefaultItem() {
        return BixisItems.IRON_MIZRAK.get();
    }

    // -------------------------------------------------------------------------
    // Fizik — trident benzeri minimal yerçekimi

    /**
     * Stuck durumunda gravity = 0 (konum sabitleme).
     * Uçuşta = 0.05f (AbstractArrow/trident ile aynı değer).
     */
    @Override
    protected float getGravity() {
        return stuck ? 0.0f : 0.05f;
    }

    /**
     * Her tick: stuck durumdaysa velocity sıfırlanır ve otomatik pickup kontrol edilir.
     *
     * <p>NOT: super.tick() içindeki local deltaMovement değişkeni, onHitBlock içinde
     * yaptığımız setDeltaMovement çağrısını override eder. Bunu önlemek için
     * super.tick() SONRASINDA tekrar sıfırlıyoruz.</p>
     */
    @Override
    public void tick() {
        super.tick();
        if (stuck) {
            this.setDeltaMovement(Vec3.ZERO);
            if (!this.level().isClientSide) {
                Entity owner = this.getOwner();
                if (owner instanceof Player player && player.distanceTo(this) < 1.5) {
                    doPickup(player);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // İsabet — blok

    /**
     * Blok ile çarpışma: mızrak saplanır, kaybolmaz.
     * Re-entry koruması: zaten stuck ise ignore.
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (stuck) return;
        stuck = true;
        this.setDeltaMovement(Vec3.ZERO);
        this.playSound(SoundEvents.TRIDENT_HIT_GROUND, 1.0f, 1.0f);
    }

    // -------------------------------------------------------------------------
    // İsabet — entity

    /**
     * Entity ile çarpışma: hasar uygular, aşağı hız verir (yere düşer), discard etmez.
     * Re-entry koruması: zaten stuck ise ignore.
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (stuck) return;
        Entity target = result.getEntity();
        Entity owner  = this.getOwner();
        target.hurt(
            this.damageSources().thrown(this, owner != null ? owner : this),
            (float) thrownDamage
        );
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0f, 1.0f);
        // Discard etme — yere düş, oradan onHitBlock ile saplanır
        this.setDeltaMovement(0.0, -0.3, 0.0);
    }

    // -------------------------------------------------------------------------
    // Pickup — sağ tık

    /**
     * Saplanmış mızrağa sağ tıkla pickup.
     * Sadece stuck iken aktif; pickedUp flag'i ile walk-over dupesi önlenir.
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide || !this.isAlive() || !stuck) return InteractionResult.PASS;
        boolean success = doPickup(player);
        return success ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    // -------------------------------------------------------------------------
    // Pickup — walk-over (otomatik, tick içinden çağrılır)

    /**
     * Ortak pickup mantığı — hem walk-over hem sağ tık buraya gelir.
     * pickedUp = true yapıldıktan sonra inventory add başarısız olursa geri alınır.
     *
     * @return true = başarılı pickup
     */
    private boolean doPickup(Player player) {
        if (pickedUp) return false;
        ItemStack item = this.getItem().copyWithCount(1);
        if (item.isEmpty()) return false;
        pickedUp = true;
        if (player.getInventory().add(item)) {
            this.playSound(SoundEvents.ITEM_PICKUP, 0.2f,
                ((this.random.nextFloat() - this.random.nextFloat()) * 0.7f + 1.0f) * 2.0f);
            this.discard();
            return true;
        }
        pickedUp = false; // Envanter doluysa geri al
        return false;
    }

    // -------------------------------------------------------------------------
    // NBT — super item serilizasyonunu yönetir; biz ek alanları ekleriz

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Stuck", stuck);
        tag.putDouble("ThrownDamage", thrownDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        stuck        = tag.getBoolean("Stuck");
        thrownDamage = tag.getDouble("ThrownDamage");
    }
}
