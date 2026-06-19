package com.bixis.bixismod.npc;

import com.bixis.bixismod.Constants;
import com.bixis.bixismod.config.BixisRatesConfig;
import com.bixis.bixismod.item.BixisItems;
import com.bixis.bixismod.weapon.BixisSounds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.Level;

/**
 * Rahim Koç — Döviz Bürosu NPC'si.
 * Oyuncudan materyal alır, karşılığında bixis:turk_lirasi verir.
 * Hareket etmez, oyuncuya bakar.
 */
public class RahimKocEntity extends AbstractVillager {

    public RahimKocEntity(EntityType<? extends RahimKocEntity> type, Level level) {
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
     * BixisRatesConfig'den okunan oranlarla döviz trade listesini oluşturur.
     * Format: VER → AL (1 materyal → N TL)
     */
    @Override
    protected void updateTrades() {
        this.getOffers().add(makeTrade(new ItemStack(Items.IRON_INGOT),      BixisRatesConfig.getIronRate()));
        this.getOffers().add(makeTrade(new ItemStack(Items.GOLD_INGOT),      BixisRatesConfig.getGoldRate()));
        this.getOffers().add(makeTrade(new ItemStack(Items.EMERALD),         BixisRatesConfig.getEmeraldRate()));
        this.getOffers().add(makeTrade(new ItemStack(Items.DIAMOND),         BixisRatesConfig.getDiamondRate()));
        this.getOffers().add(makeTrade(new ItemStack(Items.NETHERITE_INGOT), BixisRatesConfig.getNetheriteRate()));
        this.getOffers().add(makeTrade(new ItemStack(Items.ENDER_EYE),       BixisRatesConfig.getEnderEyeRate()));
        this.getOffers().add(makeTrade(new ItemStack(Items.BEACON),          BixisRatesConfig.getBeaconRate()));
        this.getOffers().add(makeTrade(new ItemStack(Items.NETHER_STAR),     BixisRatesConfig.getNetherStarRate()));
    }

    /** NPC üremez. */
    @Override
    public RahimKocEntity getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    /** Ticaret XP sistemi kullanılmadığından boş bırakılır. */
    @Override
    public void rewardTradeXp(MerchantOffer offer) {}

    @Override
    public net.minecraft.sounds.SoundEvent getNotifyTradeSound() {
        return BixisSounds.RAHIM_KOC_TRADE.get();
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

    /** Tek maliyetli trade oluşturur: 1 cost → tlAmount TL */
    private MerchantOffer makeTrade(ItemStack cost, int tlAmount) {
        return new MerchantOffer(
            cost,
            new ItemStack(BixisItems.TURK_LIRASI.get(), tlAmount),
            Constants.TRADE_MAX_USES,
            Constants.TRADE_XP,
            Constants.TRADE_PRICE_MULTIPLIER
        );
    }
}
