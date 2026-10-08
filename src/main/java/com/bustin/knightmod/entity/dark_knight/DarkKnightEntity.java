package com.bustin.knightmod.entity.dark_knight;

import com.bustin.knightmod.entity.dark_knight.attacks.BasicRangedAttack;
import com.bustin.knightmod.entity.dark_knight.util.DarkKnightPlayerAnalyzer;
import com.bustin.knightmod.entity.projectile.RunicBlastProjectile;
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
import net.minecraft.world.entity.LivingEntity;
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
    private final DarkKnightPlayerAnalyzer analyzer = new DarkKnightPlayerAnalyzer();

    // ==============================
    // ATTACKS
    // ==============================

    //Melee
    public static final String HEAVY_SLAM = "heavy_slam";
    public static final String WIDE_SWEEP = "wide_sweep";

    //Ranged
    public static final String RUNIC_BLAST = "runic_blast";

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

    @Override
    public void aiStep() {
        super.aiStep();

        if (level.isClientSide || tickCount % 20 != 0) {
            return;
        }

        LivingEntity target = getTarget();

        if (target != null && target.isAlive()) {
            analyzer.observe(distanceTo(target));

//            System.out.println(
//                    "[DARK KNIGHT] Player: " + target.getName().getString()
//                            + " | Distance: " + String.format("%.2f", distanceTo(target))
//                            + " | Detected Style: " + analyzer.getPlayerStyle()
//            );

        } else {
            analyzer.reset();
        }
    }

    // ==============================
    // ATTACK SYSTEM
    // ==============================

    //Melee
    public static final BasicMeleeAttack.BasicMeleeAttackAttributes
            HEAVY_SLAM_ATTRIBUTES = new BasicMeleeAttack.BasicMeleeAttackAttributes(
                    new BasicMeleeAttack.BasicMeleeAttackAttributes.Slice(-0.1F, 0.6F),
                    40, 24, HEAVY_SLAM, 2.0F, 10.0F
    );

    public static final BasicMeleeAttack.BasicMeleeAttackAttributes
            WIDE_SWEEP_ATTRIBUTES = new BasicMeleeAttack.BasicMeleeAttackAttributes(
            new BasicMeleeAttack.BasicMeleeAttackAttributes.Slice(0.0F, 0.2F),
            30, 15, WIDE_SWEEP, 5.0F, 0.1F
    );

    //Ranged
    public static final BasicRangedAttack.BasicRangedAttackAttributes
            RUNIC_BLAST_ATTRIBUTES = new BasicRangedAttack.BasicRangedAttackAttributes(
                    35, 0, RUNIC_BLAST, 2.7F, 0.0F, 5.0F, 18.0F, 0.65F
    );





    // FACTORIES
    public static final Map<String, BiFunction<VaultBossBaseEntity, Double, IMeleeAttack>> ATTACK_FACTORIES = Map.of(
            HEAVY_SLAM, (boss, multiplier) -> new BasicMeleeAttack(boss, multiplier, HEAVY_SLAM_ATTRIBUTES),
            WIDE_SWEEP, (boss, multiplier) -> new BasicMeleeAttack(boss, multiplier, WIDE_SWEEP_ATTRIBUTES),

            RUNIC_BLAST, (boss, multiplier) -> new BasicRangedAttack(boss, multiplier, RUNIC_BLAST_ATTRIBUTES,
                    (entity, level) -> new RunicBlastProjectile(level, entity))
    );





    @Override
    public Map<String, BiFunction<VaultBossBaseEntity, Double, IMeleeAttack>> getMeleeAttackFactories() {
        return ATTACK_FACTORIES;
    }

    @Override
    public WeightedList<AttackData> getMeleeAttacks() {

        WeightedList<AttackData> attacks = new WeightedList<>();

        LivingEntity target = getTarget();

        if (target == null) {
            return attacks;
        }

        double distanceSqr = distanceToSqr(target);

        // Ranged attack selection
        if (distanceSqr >= 25.0D) {

            if (distanceSqr <= 324.0D) {
                attacks.add(new AttackData(RUNIC_BLAST, 1.0D), 100);
            }

            return attacks;
        }

        // Close-range attack selection
        switch (analyzer.getPlayerStyle()) {

            case MELEE -> {
                attacks.add(
                        new AttackData(HEAVY_SLAM, 1.0D), 20
                );
                attacks.add(
                        new AttackData(WIDE_SWEEP, 0.75D), 80
                );
            }

            case RANGED -> {
                attacks.add(
                        new AttackData(HEAVY_SLAM, 1.0D), 70
                );
                attacks.add(
                        new AttackData(WIDE_SWEEP, 0.75D), 30
                );
            }

            case BALANCED -> {
                attacks.add(
                        new AttackData(HEAVY_SLAM, 1.0D), 50
                );
                attacks.add(
                        new AttackData(WIDE_SWEEP, 0.75D), 50
                );
            }
        }

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
