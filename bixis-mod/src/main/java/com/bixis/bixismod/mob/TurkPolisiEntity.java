package com.bixis.bixismod.mob;

import com.bixis.bixismod.item.BixisItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Türk Polisi — hostile mob, rüşvet mekaniği.
 *
 * <p>Üç state:
 * <ul>
 *   <li>HOSTILE: MainHand=netherite_sword, OffHand=shield, oyuncu + hostile mob hedefler</li>
 *   <li>RUSVET: MainHand=boş, OffHand=turk_lirasi, sadece hostile mob hedefler</li>
 *   <li>MOB_FIGHT: MainHand=netherite_sword, OffHand=turk_lirasi, passive ama dövüşüyor</li>
 * </ul>
 */
public class TurkPolisiEntity extends Monster {

    private enum PolisState { HOSTILE, RUSVET, MOB_FIGHT }

    // null: henüz initialize edilmedi — applyState(HOSTILE) ilk çağrıda geçer
    @Nullable private PolisState state = null;

    /** 0 = hostile; >0 = passive (rüşvetli), her tick azalır. */
    private int passiveTicks = 0;

    /** İlk tick'te runtime üzerinden ekipman uygular — spawn-time sync sorununu önler. */
    private boolean needsInitialEquip = true;

    private static final int RUSVET_DURATION = 3000;

    public TurkPolisiEntity(EntityType<? extends TurkPolisiEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    // -------------------------------------------------------------------------
    // Attributes

    /** @return Türk Polisi attribute builder */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 50.0)
            .add(Attributes.ATTACK_DAMAGE, 6.0)
            .add(Attributes.MOVEMENT_SPEED, 0.30)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ARMOR, 4.0);
    }

    // -------------------------------------------------------------------------
    // Goals — hostile moblar önce (priority 3), oyuncu en son (priority 5)

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Hostile mob hedefleri — priority 3 (oyuncudan önce)
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Zombie.class,     true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Skeleton.class,   true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Spider.class,     true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CaveSpider.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Creeper.class,    true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Witch.class,      true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Pillager.class,   true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Vindicator.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Evoker.class,     true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, WitherSkeleton.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Drowned.class,    true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Husk.class,       true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Stray.class,      true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, FenerbahceHoliganEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, GalatasarayHoliganEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, BesiktasHoliganEntity.class,    true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, TrabzonsporHoliganEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, HirtEntity.class, true));

        // Oyuncu hedefi — priority 5 (sadece hostile mob yoksa)
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // -------------------------------------------------------------------------
    // Spawn — ekipman ve drop şansı

    /**
     * Spawn tracking paketine ekipmanı dahil eder.
     * finalizeSpawn'dan önce çağrılır; server→client sync için kritik.
     */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        this.setItemSlot(EquipmentSlot.MAINHAND, makeSword());
        this.setItemSlot(EquipmentSlot.OFFHAND,  new ItemStack(Items.SHIELD));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag) {
        // super içinde populateDefaultEquipmentSlots çağrılır — ekipman orada set edildi
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData, tag);
        // state'i HOSTILE olarak işaretle (populateDefaultEquipmentSlots zaten ekipmanı koydu)
        state = PolisState.HOSTILE;
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        this.setDropChance(EquipmentSlot.OFFHAND,  0.0f);
        this.setDropChance(EquipmentSlot.HEAD,     0.0f);
        this.setDropChance(EquipmentSlot.CHEST,    0.0f);
        this.setDropChance(EquipmentSlot.LEGS,     0.0f);
        this.setDropChance(EquipmentSlot.FEET,     0.0f);
        return data;
    }

    // -------------------------------------------------------------------------
    // State geçişleri — sadece state değişince ekipman güncellenir

    private void applyState(PolisState next) {
        if (state == next) return;
        state = next;
        switch (next) {
            case HOSTILE -> {
                ItemStack sword = makeSword();
                this.setItemSlot(EquipmentSlot.MAINHAND, sword);
                this.setItemSlot(EquipmentSlot.OFFHAND,  new ItemStack(Items.SHIELD));
            }
            case RUSVET -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.setItemSlot(EquipmentSlot.OFFHAND,  new ItemStack(BixisItems.TURK_LIRASI.get()));
            }
            case MOB_FIGHT -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, makeSword());
                this.setItemSlot(EquipmentSlot.OFFHAND,  new ItemStack(BixisItems.TURK_LIRASI.get()));
            }
        }
    }

    private ItemStack makeSword() {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 2);
        sword.enchant(Enchantments.KNOCKBACK, 2);
        return sword;
    }

    // -------------------------------------------------------------------------
    // aiStep — rüşvet tespiti + state makinesi

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        // İlk tick: spawn-time sync bypass — runtime üzerinden ekipman uygula
        if (needsInitialEquip) {
            needsInitialEquip = false;
            PolisState current = (state != null) ? state : PolisState.HOSTILE;
            state = null; // applyState guard'ını bypass et
            applyState(current);
        }

        // Her modda 4 blok içinde turk_lirasi item entity ara
        List<ItemEntity> tlItems = this.level().getEntitiesOfClass(
            ItemEntity.class,
            this.getBoundingBox().inflate(4),
            ie -> ie.getItem().is(BixisItems.TURK_LIRASI.get().asItem())
        );

        if (!tlItems.isEmpty()) {
            tlItems.get(0).discard();
            if (passiveTicks > 0) {
                passiveTicks = RUSVET_DURATION; // Süreyi sıfırla, state değişmez
            } else {
                startPassive();
            }
            return;
        }

        if (passiveTicks > 0) {
            // Oyuncu hedefini engelle
            LivingEntity target = this.getTarget();
            if (target instanceof Player) {
                this.setTarget(null);
                target = null;
            }

            // Hostile mob hedef durumuna göre state geçişi (sadece değişince)
            if (target != null) {
                applyState(PolisState.MOB_FIGHT);
            } else {
                applyState(PolisState.RUSVET);
            }

            passiveTicks--;
            if (passiveTicks == 0) {
                applyState(PolisState.HOSTILE);
                Player nearest = this.level().getNearestPlayer(this, 32.0);
                if (nearest != null) this.setTarget(nearest);
            }
        }
    }

    /** Rüşvet alındı — RUSVET state'e geç, yakın oyunculara bildir. */
    private void startPassive() {
        passiveTicks = RUSVET_DURATION;
        this.setTarget(null);
        applyState(PolisState.RUSVET);

        Component msg = Component.literal("Rüşvet alındı.");
        this.level().getEntitiesOfClass(
            ServerPlayer.class,
            this.getBoundingBox().inflate(16),
            p -> true
        ).forEach(sp -> sp.displayClientMessage(msg, true));
    }

    // -------------------------------------------------------------------------
    // NBT — passiveTicks ve state kaydet/yükle

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PassiveTicks", passiveTicks);
        tag.putString("PolisState", state.name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        passiveTicks = tag.getInt("PassiveTicks");
        String savedState = tag.getString("PolisState");
        try {
            state = savedState.isEmpty() ? PolisState.HOSTILE : PolisState.valueOf(savedState);
        } catch (IllegalArgumentException e) {
            state = PolisState.HOSTILE;
        }
    }

    // -------------------------------------------------------------------------
    // Sesler — hepsi null

    @Override @Nullable protected SoundEvent getAmbientSound() { return null; }
    @Override @Nullable protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override @Nullable protected SoundEvent getDeathSound() { return null; }
}
