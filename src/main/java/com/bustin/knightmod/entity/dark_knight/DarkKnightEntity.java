package com.bustin.knightmod.entity.dark_knight;

import iskallia.vault.core.util.WeightedList;
import iskallia.vault.entity.boss.VaultBossBaseEntity;
import iskallia.vault.entity.boss.attack.BasicMeleeAttack;
import iskallia.vault.entity.boss.attack.BossMeleeAttackGoal;
import iskallia.vault.entity.boss.attack.IMeleeAttack;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Map;
import java.util.function.BiFunction;

public class DarkKnightEntity extends VaultBossBaseEntity implements IAnimatable {

    // ==============================
    // ATTACKS
    // ==============================

    public static final String HEAVY_SLAM = "heavy_slam";

    // ==============================
    // ANIMATIONS
    // ==============================

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    // ==============================
    // CONSTRUCTOR
    // ==============================

    public DarkKnightEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // ==============================
    // ATTRIBUTES
    // ==============================

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    // ==============================
    // AI GOALS
    // ==============================

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(2, new BossMeleeAttackGoal(this)
        );

        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F)
        );

        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this)
        );

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true)
        );

        // Attack goal will be added later.
    }

    // ==============================
    // ATTACK SYSTEM
    // ==============================

    public static final BasicMeleeAttack.BasicMeleeAttackAttributes
            HEAVY_SLAM_ATTRIBUTES = new BasicMeleeAttack.BasicMeleeAttackAttributes(
                    new BasicMeleeAttack.BasicMeleeAttackAttributes.Slice(-0.1F, 0.6F),
                    40, 24, HEAVY_SLAM, 2.0F, 10.0F
            );



    // FACTORIES
    public static final Map<String, BiFunction<VaultBossBaseEntity, Double, IMeleeAttack>> ATTACK_FACTORIES = Map.of(
            HEAVY_SLAM, (boss, multiplier) -> new BasicMeleeAttack(boss, multiplier, HEAVY_SLAM_ATTRIBUTES)
    );





    @Override
    public Map<String, BiFunction<VaultBossBaseEntity, Double, IMeleeAttack>> getMeleeAttackFactories() {
        return ATTACK_FACTORIES;
    }

    @Override
    public WeightedList<AttackData> getMeleeAttacks() {
        WeightedList<AttackData> attacks = new WeightedList<>();

        attacks.add(new AttackData(HEAVY_SLAM, 1.0D), 100);

        return attacks;
    }

    @Override
    public WeightedList<AttackData> getRageAttacks() {
        return WeightedList.empty();
    }

    @Override
    public double getAttackReach() {
        return 4.0D;
    }

    // ==============================
    // BOSS BAR
    // ==============================

    @Override
    public ServerBossEvent getServerBossInfo() {
        return new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    }

    // ==============================
    // SOUND
    // ==============================

    @Override
    public void playAttackSound() {
        level.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0F, 0.8F);
    }

    // ==============================
    // GECKOLIB
    // ==============================

    @Override
    public void registerControllers(AnimationData data) {
        // Add idle, walking and attack animations later.
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
