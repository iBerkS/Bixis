package com.bixis.bixismod.npc;

import com.bixis.bixismod.BixisMod;
import com.bixis.bixismod.Constants;
import com.bixis.bixismod.config.BixisRatesConfig;
import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Villa Hakan — Silah Dükkanı NPC'si.
 * Oyuncudan bixis:turk_lirasi alır, silah/ekipman verir.
 * Hareket etmez, oyuncuya bakar.
 */
public class VillaHakanEntity extends AbstractVillager {

    public VillaHakanEntity(EntityType<? extends VillaHakanEntity> type, Level level) {
        super(type, level);
        this.setNoAi(false);
    }

    /**
     * Sağ tıkta trading GUI'yi açar.
     * AbstractVillager 1.20.1'de mobInteract override etmediğinden burada yazılır.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (!this.level().isClientSide) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    /** Sadece LookAt hedefleri kayıt edilir; hareket hedefi eklenmez. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, Constants.NPC_LOOK_RANGE));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    /**
     * BixisRatesConfig'den okunan fiyatlarla shop trade listesini oluşturur.
     * Format: VER → AL (N TL → item)
     */
    @Override
    protected void updateTrades() {
        this.getOffers().add(buyWithTl(BixisRatesConfig.getFenerbahceFormaPrice(),
            new ItemStack(BixisItems.FENERBAHCE_FORMA.get())));

        this.getOffers().add(buyWithTl(BixisRatesConfig.getOtomatikTufekPrice(),
            new ItemStack(BixisItems.M4.get())));

        this.getOffers().add(buyWithTl(BixisRatesConfig.getYariOtomatikTufekPrice(),
            new ItemStack(BixisItems.AWP.get())));

        this.getOffers().add(buyWithTl(BixisRatesConfig.getTabancaPrice(),
            new ItemStack(BixisItems.DESERT_EAGLE.get())));

        this.getOffers().add(buyWithTl(BixisRatesConfig.getMermiPrice(),
            new ItemStack(BixisItems.MERMI.get(), Constants.MERMI_PER_TRADE)));

        this.getOffers().add(buyWithTl(BixisRatesConfig.getFenerbahceKiliciPrice(),
            new ItemStack(BixisItems.FENERBAHCE_KILICI.get())));

        this.getOffers().add(buyWithTl(BixisRatesConfig.getTcPasaportuPrice(),
            new ItemStack(BixisItems.TC_PASAPORTU.get())));

        ItemStack luckyPotion = getLuckyPotion();
        if (!luckyPotion.isEmpty()) {
            this.getOffers().add(buyWithTl(BixisRatesConfig.getLuckyPotionPrice(), luckyPotion));
        }
    }

    /** NPC üremez. */
    @Override
    public VillaHakanEntity getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    /** Ticaret XP sistemi kullanılmadığından boş bırakılır. */
    @Override
    public void rewardTradeXp(MerchantOffer offer) {}

    @Override
    public net.minecraft.sounds.SoundEvent getNotifyTradeSound() {
        return BixisSounds.VILLA_HAKAN_TRADE.get();
    }

    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s) { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return null; }

    /**
     * NPC entity attribute'larını tanımlar.
     *
     * @return Forge attribute builder
     */
    public static AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    // -------------------------------------------------------------------------

    /** TL ödeyip item alan trade: N TL → result */
    private MerchantOffer buyWithTl(int tlCost, ItemStack result) {
        return new MerchantOffer(
            new ItemStack(BixisItems.TURK_LIRASI.get(), tlCost),
            result,
            Constants.TRADE_MAX_USES,
            Constants.TRADE_XP,
            Constants.TRADE_PRICE_MULTIPLIER
        );
    }

    /**
     * Lucky Block mod yüklüyse "ProteinOşın" isimli lucky:lucky_potion stack'i döner, yoksa boş stack.
     */
    private ItemStack getLuckyPotion() {
        @SuppressWarnings("removal")
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("lucky:lucky_potion"));
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            BixisMod.LOGGER.warn("lucky:lucky_potion bulunamadı — Lucky Block modu yüklü mü?");
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, Constants.LUCKY_POTION_PER_TRADE);
        stack.setHoverName(Component.literal("ProteinOşın"));
        return stack;
    }
}
