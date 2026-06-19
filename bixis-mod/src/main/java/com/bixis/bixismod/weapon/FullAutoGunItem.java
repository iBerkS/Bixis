package com.bixis.bixismod.weapon;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.RegistryObject;

/**
 * Tam otomatik silah — sağ tık basılı tutulurken her {@code fireRateTicks} tick'te bir
 * {@link #fireOnce(ServerPlayer, ItemStack)} çağrılır.
 *
 * <p>İlk ateş {@code use()} içinde anında gerçekleşir; sonraki ateşler
 * {@code onUseTick()} ile sürer. Şarjör bitince reload başlar ve
 * oyuncu sağ tığı bırakana kadar reload ekranı gösterilir.</p>
 *
 * <p>{@code getUseDuration()} = 72000 → sağ tık bırakılana kadar devam eder.
 * Animasyon: {@link UseAnim#NONE} — yeme animasyonu oynanmaz.</p>
 */
public class FullAutoGunItem extends GunItem {

    /** Tick cinsinden ateşler arası süre (M4: 2 tick → 10 atış/sn). */
    private final int fireRateTicks;

    /**
     * @param magazineSize  şarjör kapasitesi
     * @param reloadTicks   reload süresi (tick)
     * @param throwSpeed    mermi fırlatma hızı
     * @param bulletDamage  mermi hasarı
     * @param fireRateTicks ateşler arası süre (tick)
     * @param gunName       görünen isim (actionbar)
     * @param fireSound     ateş sesi
     * @param emptySound    boş tetik sesi
     * @param equipSound    kuşanma sesi
     * @param reloadSound   reload başlangıç sesi
     * @param props         item özellikleri
     */
    public FullAutoGunItem(int magazineSize, int reloadTicks, float throwSpeed, float bulletDamage,
                           int fireRateTicks, String gunName,
                           RegistryObject<SoundEvent> fireSound, RegistryObject<SoundEvent> emptySound,
                           RegistryObject<SoundEvent> equipSound, RegistryObject<SoundEvent> reloadSound,
                           Properties props) {
        super(magazineSize, reloadTicks, throwSpeed, bulletDamage,
              0 /* full-auto, cooldown yönetimi fireRateTicks ile yapılır */,
              gunName, fireSound, emptySound, equipSound, reloadSound, props);
        this.fireRateTicks = fireRateTicks;
    }

    // -------------------------------------------------------------------------
    // Full-auto use mekanizması

    /**
     * Sağ tıkta: anında ilk ateş, sonrasında onUseTick devam eder.
     * Şarjör boşsa reload başlatır ve "use" moduna girmez.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (GunEventHandler.isReloading(sp.getUUID())) {
                // Reload sırasında use moduna gir ama ateş etme
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
            }
            int ammo = getAmmo(stack);
            if (ammo <= 0) {
                tryStartReload(sp, stack);
                return InteractionResultHolder.pass(stack);
            }
            fireOnce(sp, stack); // Anında ilk atış
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    /**
     * Her tick çağrılır. Elapsed % fireRateTicks == 0 ise ateş eder.
     * (Elapsed = 0 anı use() içinde ele alındı; burası 1'den başlar.)
     */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) return;

        int elapsed = getUseDuration(stack) - remainingUseDuration;
        if (elapsed <= 0 || elapsed % fireRateTicks != 0) return;

        if (GunEventHandler.isReloading(player.getUUID())) return;

        int ammo = getAmmo(stack);
        if (ammo <= 0) {
            tryStartReload(player, stack);
            return;
        }
        fireOnce(player, stack);
    }

    /** 72000 tick — sağ tık bırakılana kadar aktif kalır. */
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    /** Yeme animasyonunu gizler. */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }
}
