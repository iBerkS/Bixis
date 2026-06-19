package com.bixis.bixismod.weapon;

import com.bixis.bixismod.item.BixisItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

/**
 * Temel silah sınıfı — yarı otomatik, şarjör + reload sistemi.
 * NBT anahtarı {@value #NBT_AMMO}: int, şarjördeki mermi sayısı.
 *
 * <p>Full-auto varyant için {@link FullAutoGunItem} extend eder.</p>
 *
 * <p>Ateş akışı (sağ tık):
 * <ol>
 *   <li>Reload sürüyorsa işlem yapma</li>
 *   <li>Shotcooldown varsa işlem yapma</li>
 *   <li>Şarjör doluysa: mermi ateşle, BixisAmmo--, eğer bitti → auto-reload</li>
 *   <li>Şarjör boşsa: envanterde yeterli mermi varsa reload başlat,
 *       yoksa "Mermi yok!" actionbar mesajı</li>
 * </ol>
 *
 * <p>Sol tık reload: {@link GunEventHandler} {@code AttackEntityEvent} /
 * {@code LeftClickBlock} / {@code LeftClickEmpty} yakalayıp
 * {@link #tryStartReload(ServerPlayer, ItemStack)} çağırır.</p>
 */
public class GunItem extends Item {

    /** ItemStack NBT anahtarı — şarjördeki mermi sayısı. */
    public static final String NBT_AMMO = "BixisAmmo";

    protected final int    magazineSize;
    protected final int    reloadTicks;
    protected final float  throwSpeed;
    protected final float  bulletDamage;
    /** Ateşler arası zorunlu bekleme (tick). AWP: 20, DE: 5. */
    protected final int    shotCooldownTicks;
    /** Actionbar "hazır!" mesajında ve ses olaylarında kullanılan görünen isim. */
    protected final String gunName;

    protected final RegistryObject<SoundEvent> fireSound;
    protected final RegistryObject<SoundEvent> emptySound;
    protected final RegistryObject<SoundEvent> equipSound;
    protected final RegistryObject<SoundEvent> reloadSound;

    /**
     * @param magazineSize      şarjör kapasitesi
     * @param reloadTicks       reload süresi (tick)
     * @param throwSpeed        mermi fırlatma hızı
     * @param bulletDamage      mermi hasarı
     * @param shotCooldownTicks ateşler arası cooldown (tick)
     * @param gunName           görünen isim (actionbar için)
     * @param fireSound         ateş sesi
     * @param emptySound        boş tetik sesi
     * @param equipSound        kuşanma sesi
     * @param reloadSound       reload başlangıç sesi
     * @param props             item özellikleri
     */
    public GunItem(int magazineSize, int reloadTicks, float throwSpeed, float bulletDamage,
                   int shotCooldownTicks, String gunName,
                   RegistryObject<SoundEvent> fireSound, RegistryObject<SoundEvent> emptySound,
                   RegistryObject<SoundEvent> equipSound, RegistryObject<SoundEvent> reloadSound,
                   Properties props) {
        super(props);
        this.magazineSize      = magazineSize;
        this.reloadTicks       = reloadTicks;
        this.throwSpeed        = throwSpeed;
        this.bulletDamage      = bulletDamage;
        this.shotCooldownTicks = shotCooldownTicks;
        this.gunName           = gunName;
        this.fireSound         = fireSound;
        this.emptySound        = emptySound;
        this.equipSound        = equipSound;
        this.reloadSound       = reloadSound;
    }

    // -------------------------------------------------------------------------
    // Şarjör NBT API

    /**
     * Şarjördeki mevcut mermi sayısını döndürür.
     * NBT yoksa (item yeni) şarjör dolu kabul edilir.
     *
     * @param stack silah item stack'i
     * @return 0..magazineSize arası değer
     */
    public int getAmmo(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(NBT_AMMO)) ? tag.getInt(NBT_AMMO) : magazineSize;
    }

    /**
     * Şarjördeki mermi sayısını ayarlar.
     *
     * @param stack silah item stack'i
     * @param ammo  0..magazineSize
     */
    public void setAmmo(ItemStack stack, int ammo) {
        stack.getOrCreateTag().putInt(NBT_AMMO, ammo);
    }

    // -------------------------------------------------------------------------
    // Sağ tık — ateş (yarı otomatik)

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.pass(stack);

        if (GunEventHandler.isReloading(sp.getUUID())) return InteractionResultHolder.pass(stack);
        if (player.getCooldowns().isOnCooldown(this))  return InteractionResultHolder.pass(stack);

        int ammo = getAmmo(stack);
        if (ammo <= 0) {
            tryStartReload(sp, stack);
            return InteractionResultHolder.pass(stack);
        }

        fireOnce(sp, stack);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    // -------------------------------------------------------------------------
    // Ateş mantığı — FullAutoGunItem tarafından da çağrılır

    /**
     * Tek bir mermi fırlatır, BixisAmmo günceller, gerekirse auto-reload tetikler.
     *
     * @param player ateş eden oyuncu (server-side)
     * @param stack  elde tutulan silah stack'i
     */
    protected void fireOnce(ServerPlayer player, ItemStack stack) {
        BulletProjectileEntity bullet = new BulletProjectileEntity(player.level(), player, bulletDamage);
        bullet.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, throwSpeed, 0.5f);
        player.level().addFreshEntity(bullet);

        player.level().playSound(null, player.blockPosition(),
            fireSound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        int newAmmo = getAmmo(stack) - 1;
        setAmmo(stack, newAmmo);
        player.getInventory().setChanged();

        if (shotCooldownTicks > 0) {
            player.getCooldowns().addCooldown(this, shotCooldownTicks);
        }

        if (newAmmo <= 0) {
            tryStartReload(player, stack);
        }
    }

    // -------------------------------------------------------------------------
    // Sol tık — IForgeItem overrides (server-side, her swing için güvenilir)

    /**
     * Herhangi bir sol tık (entity, blok, hava) → swing olarak yakalanır.
     * Şarjör dolmamışsa reload başlatır ve swing animasyonunu bastırır.
     *
     * <p>{@code LeftClickEmpty}/{@code LeftClickBlock} event'lerinin aksine bu
     * override sunucu tarafında {@code ServerboundSwingPacket} işlendiğinde
     * güvenilir biçimde tetiklenir.</p>
     *
     * @return true = swing bastır (reload başladı veya reload devam ediyor)
     */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity.level().isClientSide) return false;
        if (!(entity instanceof ServerPlayer sp)) return false;
        if (GunEventHandler.isReloading(sp.getUUID())) return true; // reload sırasında swing bastır
        int ammo = getAmmo(stack);
        if (ammo <= 0) {
            tryStartReload(sp, stack);
            return true; // şarjör boş, reload başlat
        }
        return false; // şarjörde mermi var, normal swing
    }

    /**
     * Blok kırmaya başlanınca: reload varsa veya şarjör dolu değilse blok kırmayı engelle.
     * Reload tetiklemesi {@link #onEntitySwing} üzerinden zaten gerçekleşir.
     *
     * @return true = blok kırmayı engelle
     */
    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level().isClientSide) return false;
        if (!(player instanceof ServerPlayer sp)) return false;
        if (GunEventHandler.isReloading(sp.getUUID())) return true;
        return getAmmo(stack) <= 0;
    }

    // -------------------------------------------------------------------------
    // Reload

    /**
     * En az 1 mermi varsa reload başlatır.
     * Reload sonunda gerçekte tüketilen mermi kadar şarjör dolar (kısmi doldurma).
     * Yoksa "Mermi yok!" actionbar + boş tetik sesi.
     * Package-private: {@link GunEventHandler} tarafından da çağrılır.
     */
    void tryStartReload(ServerPlayer player, ItemStack stack) {
        if (GunEventHandler.isReloading(player.getUUID())) return;
        if (countMermi(player) < 1) {
            player.displayClientMessage(Component.literal("Mermi yok!"), true);
            player.level().playSound(null, player.blockPosition(),
                emptySound.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
            return;
        }
        GunEventHandler.startReload(player.getUUID(), reloadTicks);
        player.level().playSound(null, player.blockPosition(),
            reloadSound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /** Envanterden bixis:mermi toplam sayısını sayar. */
    protected int countMermi(Player player) {
        return player.getInventory().items.stream()
            .filter(s -> s.is(BixisItems.MERMI.get()))
            .mapToInt(ItemStack::getCount)
            .sum();
    }

    // -------------------------------------------------------------------------
    // Getters (GunEventHandler için)

    /** @return şarjör kapasitesi */
    public int getMagazineSize()           { return magazineSize; }
    /** @return reload süresi (tick) */
    public int getReloadTicks()            { return reloadTicks;  }
    /** @return actionbar'da görünen silah adı */
    public String getGunName()             { return gunName;      }
    /** @return kuşanma sesi */
    public SoundEvent getEquipSound()      { return equipSound.get(); }
}
