package com.bixis.bixismod.weapon;

import com.bixis.bixismod.item.BixisItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Silah event handler — reload tick sayacı, actionbar göstergeleri, kuşanma sesi,
 * sol tık reload tetikleyici.
 *
 * <p>Tüm state sunucu-side statik map'lerde tutulur; logout'ta temizlenir.</p>
 *
 * <p>Reload akışı:
 * <ol>
 *   <li>{@link GunItem#use()} veya sol tık → {@link #startReload(UUID, int)}</li>
 *   <li>Her {@code onPlayerTick}: sayaç azalır, actionbar gösterilir</li>
 *   <li>Sayaç sıfırlanınca: mermi düşer, BixisAmmo dolar, "hazır!" mesajı 60 tick</li>
 *   <li>Oyuncu silahı bırakırsa reload iptal edilir</li>
 * </ol>
 */
public final class GunEventHandler {

    private static final Map<UUID, Integer> reloadTicksLeft   = new HashMap<>();
    private static final Map<UUID, Integer> readyDisplayTicks = new HashMap<>();
    private static final Map<UUID, Boolean> wasHoldingGun     = new HashMap<>();

    // -------------------------------------------------------------------------
    // Public API

    /** @return true = reload devam ediyor */
    public static boolean isReloading(UUID uuid) {
        return reloadTicksLeft.getOrDefault(uuid, 0) > 0;
    }

    /**
     * Reload sayacını başlatır.
     *
     * @param uuid  oyuncu UUID'si
     * @param ticks toplam reload süresi
     */
    public static void startReload(UUID uuid, int ticks) {
        reloadTicksLeft.put(uuid, ticks);
    }

    // -------------------------------------------------------------------------
    // Tick event — reload sayacı, actionbar, kuşanma sesi

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        UUID uuid = player.getUUID();
        ItemStack stack = player.getMainHandItem();
        boolean holdingGun = stack.getItem() instanceof GunItem;

        // Kuşanma sesi: bu tick ilk kez silah elde alındıysa
        boolean wasGun = wasHoldingGun.getOrDefault(uuid, false);
        if (holdingGun && !wasGun) {
            GunItem gun = (GunItem) stack.getItem();
            player.level().playSound(null, player.blockPosition(),
                gun.getEquipSound(), SoundSource.PLAYERS, 0.7f, 1.0f);
        }
        wasHoldingGun.put(uuid, holdingGun);

        // Silahı bırakınca reload iptal et
        if (!holdingGun) {
            reloadTicksLeft.remove(uuid);
            readyDisplayTicks.remove(uuid);
            return;
        }

        GunItem gun = (GunItem) stack.getItem();

        // --- Reload tik ---
        int ticks = reloadTicksLeft.getOrDefault(uuid, 0);
        if (ticks > 0) {
            ticks--;
            reloadTicksLeft.put(uuid, ticks);
            int secondsLeft = Math.max(1, (int) Math.ceil(ticks / 20.0));
            serverPlayer.displayClientMessage(
                Component.literal("Şarjör dolduruluyor... (" + secondsLeft + "sn)"), true);

            if (ticks == 0) {
                reloadTicksLeft.remove(uuid);
                // Kısmi doldurma: envanterdeki mevcut mermi kadar yükle
                int consumed = consumeMermi(player, gun.getMagazineSize());
                gun.setAmmo(stack, consumed);
                player.getInventory().setChanged();
                readyDisplayTicks.put(uuid, 60);
            }
            return;
        }

        // --- "Hazır!" göstergesi ---
        int rem = readyDisplayTicks.getOrDefault(uuid, 0);
        if (rem > 0) {
            int ammo = gun.getAmmo(stack);
            int mag  = gun.getMagazineSize();
            serverPlayer.displayClientMessage(
                Component.literal(gun.getGunName() + " hazır! " + buildBar(ammo, mag) + " " + ammo + "/" + mag),
                true);
            readyDisplayTicks.put(uuid, rem - 1);
        }
    }

    // -------------------------------------------------------------------------
    // Sol tık — entity saldırısını engelle
    //
    // NOT: Reload tetiklemesi GunItem.onEntitySwing() (IForgeItem) üzerinden yapılır.
    // Bu handler sadece entity'e verilen hasarı engeller; saldırı animasyonunu değil.

    /**
     * Entity saldırısını hasar vermeden önce yakalar.
     * Reload sırasında veya şarjör dolu değilse hasarı iptal eder.
     * Reload tetiklemesi {@link GunItem#onEntitySwing} tarafından zaten yapılır.
     */
    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player.getMainHandItem().getItem() instanceof GunItem gun)) return;
        if (!(player instanceof ServerPlayer sp)) return;

        if (isReloading(sp.getUUID())) {
            event.setCanceled(true);
            return;
        }
        // Şarjör boşsa saldırıyı engelle (reload onEntitySwing'den tetiklendi)
        if (gun.getAmmo(player.getMainHandItem()) <= 0) {
            event.setCanceled(true);
        }
    }

    // -------------------------------------------------------------------------
    // Logout temizliği

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        reloadTicksLeft.remove(uuid);
        readyDisplayTicks.remove(uuid);
        wasHoldingGun.remove(uuid);
    }

    // -------------------------------------------------------------------------
    // Yardımcılar

    /**
     * Envanterden en fazla {@code amount} kadar bixis:mermi kaldırır.
     *
     * @return gerçekte tüketilen mermi sayısı (envanter yetersizse amount'tan az olabilir)
     */
    private int consumeMermi(Player player, int amount) {
        int remaining = amount;
        for (ItemStack s : player.getInventory().items) {
            if (remaining <= 0) break;
            if (!s.is(BixisItems.MERMI.get())) continue;
            int take = Math.min(s.getCount(), remaining);
            s.shrink(take);
            remaining -= take;
        }
        return amount - remaining;
    }

    /** [■■■□□□□] formatında mermi barı. */
    private String buildBar(int current, int max) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < max; i++) sb.append(i < current ? "■" : "□");
        sb.append("]");
        return sb.toString();
    }

    private GunEventHandler() {}

    /** Singleton — {@code MinecraftForge.EVENT_BUS.register(GunEventHandler.INSTANCE)} */
    public static final GunEventHandler INSTANCE = new GunEventHandler();
}
