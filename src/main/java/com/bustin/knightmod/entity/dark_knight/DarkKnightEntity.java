package com.bustin.knightmod.entity.dark_knight;

import com.bustin.knightmod.entity.dark_knight.attacks.BasicAbilityAttack;
import com.bustin.knightmod.entity.dark_knight.attacks.BeamAttack;
import com.bustin.knightmod.entity.dark_knight.attacks.BasicRangedAttack;
import com.bustin.knightmod.entity.dark_knight.util.DarkKnightPlayerAnalyzer;
import com.bustin.knightmod.entity.projectile.RunicBlastProjectile;
import iskallia.vault.core.util.WeightedList;
import iskallia.vault.entity.boss.VaultBossBaseEntity;
import iskallia.vault.entity.boss.attack.BasicMeleeAttack;
import iskallia.vault.entity.boss.attack.BossMeleeAttackGoal;
import iskallia.vault.entity.boss.attack.IMeleeAttack;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.ClientboundEarthquakeRippleMessage;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
    public static final String LOCK_ON_BEAM = "lock_on_beam";

    //Other
    public static final String EARTH_QUAKE = "earth_quake";

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
//                    "[DARK KNIGHT]"
//                            + " | Distance Style: " + analyzer.getPlayerStyle()
//                            + " | Aggression: " + analyzer.getAggressionLevel()
//                            + " | Damage Events: " + analyzer.getRecentDamageEvents()
//            );

        } else {
            analyzer.reset();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {

        boolean wasHurt = super.hurt(source, amount);

        if (!level.isClientSide && wasHurt
                && source.getEntity() instanceof Player) {

            analyzer.recordDamageEvent(level.getGameTime());
        }

        return wasHurt;
    }

    // ==============================
    // ATTACK SYSTEM
    // ==============================

    //Melee
    public static final BasicMeleeAttack.BasicMeleeAttackAttributes
            HEAVY_SLAM_ATTRIBUTES = new BasicMeleeAttack.BasicMeleeAttackAttributes(
                    new BasicMeleeAttack.BasicMeleeAttackAttributes.Slice(-0.1F, 0.6F),
                    40, 24, HEAVY_SLAM, 2.0F, 5.0F
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

    public static final BeamAttack.BeamAttackAttributes
            LOCK_ON_BEAM_ATTRIBUTES = new BeamAttack.BeamAttackAttributes(
                    80, 60, LOCK_ON_BEAM, 40.0F, 6.0F, 25.0F, 0.7F, 0.45F
    );

    //Other
    public static final BasicAbilityAttack.BasicAbilityAttackAttributes
            EARTH_QUAKE_ATTRIBUTES = new BasicAbilityAttack.BasicAbilityAttackAttributes(
                    50, 25, EARTH_QUAKE, 30.0F, 0.0F, 7.0F
    );


    // FACTORIES
    public static final Map<String, BiFunction<VaultBossBaseEntity, Double, IMeleeAttack>> ATTACK_FACTORIES = Map.of(
            HEAVY_SLAM, (boss, multiplier) -> new BasicMeleeAttack(boss, multiplier, HEAVY_SLAM_ATTRIBUTES),
            WIDE_SWEEP, (boss, multiplier) -> new BasicMeleeAttack(boss, multiplier, WIDE_SWEEP_ATTRIBUTES),

            RUNIC_BLAST, (boss, multiplier) -> new BasicRangedAttack(boss, multiplier, RUNIC_BLAST_ATTRIBUTES,
                    (entity, level) -> new RunicBlastProjectile(level, entity)),

            LOCK_ON_BEAM, (boss, multiplier) -> new BeamAttack(boss, multiplier, LOCK_ON_BEAM_ATTRIBUTES),

            EARTH_QUAKE, (boss, multiplier) -> new BasicAbilityAttack(
                    boss, multiplier, EARTH_QUAKE_ATTRIBUTES, DarkKnightEntity::castEarthquake)
    );

    /**
     * Boss-safe example based on Vault Hunters' Earthquake ability: damage and knock back
     * nearby enemies, then emit ground particles from the cast position.
     */
    private static void castEarthquake(VaultBossBaseEntity caster, LivingEntity target, float damage) {
        if (!(caster.level instanceof ServerLevel level)) {
            return;
        }

        double radius = EARTH_QUAKE_ATTRIBUTES.maxRange();
        Vec3 center = caster.position();

        // This is the packet used by Vault Hunters' EarthquakeAbility. Its client handler
        // feeds EarthquakeRippleRenderer, producing the expanding ground ring.
        ModNetwork.sendTrackingChunk(
                new ClientboundEarthquakeRippleMessage(center, (float) radius, 0, false),
                level.getChunkAt(caster.blockPosition())
        );

        level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(radius, 2.0D, radius),
                entity -> entity != caster && entity.isAlive() && !caster.isAlliedTo(entity)
        ).stream().filter(entity -> entity.position().distanceToSqr(center) <= radius * radius).forEach(entity -> {
            if (entity.hurt(DamageSource.mobAttack(caster), damage)) {
                Vec3 knockback = entity.position().subtract(center).multiply(1.0D, 0.0D, 1.0D);
                if (knockback.lengthSqr() > 1.0E-4D) {
                    knockback = knockback.normalize();
                    entity.push(knockback.x * 1.5D, 0.45D, knockback.z * 1.5D);
                }
            }
        });

        BlockState ground = level.getBlockState(caster.blockPosition().below());
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, ground),
                center.x, center.y + 0.1D, center.z,
                80, radius * 0.5D, 0.25D, radius * 0.5D, 0.15D
        );
        level.playSound(null, caster.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE, 1.5F, 0.65F);
    }

    @Override
    public Map<String, BiFunction<VaultBossBaseEntity, Double, IMeleeAttack>> getMeleeAttackFactories() {
        return ATTACK_FACTORIES;
    }

    @Override
    public WeightedList<AttackData> getMeleeAttacks() {

        WeightedList<AttackData> attacks = new WeightedList<>();

        LivingEntity target = getTarget();

        if (target == null || !target.isAlive()) {
            return attacks;
        }

        double distanceSqr = distanceToSqr(target);

        // Get the player's learned behaviors
        var style = analyzer.getPlayerStyle();
        var aggression = analyzer.getAggressionLevel();

        // Base attack weights
        int heavySlamWeight = 35;
        int wideSweepWeight = 35;
        int earthquakeWeight = 15;
        int runicBlastWeight = 15;
        int lockOnBeamWeight = 10;

        // ==========================
        // DISTANCE PREFERENCE
        // ==========================

        switch (style) {

            case MELEE -> {
                wideSweepWeight += 20;
                earthquakeWeight += 15;
            }

            case RANGED -> {
                runicBlastWeight += 35;
                lockOnBeamWeight += 25;
            }

            case BALANCED -> {
                // Keep default weights
            }
        }

        // ==========================
        // AGGRESSION
        // ==========================

        switch (aggression) {

            case AGGRESSIVE -> {
                // Pressure players who frequently attack
                wideSweepWeight += 15;
                earthquakeWeight += 20;
            }

            case PASSIVE -> {
                // Favor deliberate, punishable attacks
                heavySlamWeight += 20;
                runicBlastWeight += 10;
                lockOnBeamWeight += 10;
            }

            case MODERATE -> {
                // Keep default weights
            }
        }

        // ==========================
        // CURRENT RANGE
        // ==========================

        // The current BossMeleeAttackGoal uses getAttackReach()
        // as a squared-distance threshold when starting melee attacks.
        // With getAttackReach() = 4, melee starts within ~2 blocks.

        if (distanceSqr <= getAttackReach()) {

            attacks.add(
                    new AttackData(HEAVY_SLAM, 1.0D),
                    heavySlamWeight
            );

            attacks.add(
                    new AttackData(WIDE_SWEEP, 0.75D),
                    wideSweepWeight
            );
        }

        // Earthquake eligibility depends on BasicAbilityAttack.start().
        // This assumes it accepts targets within its configured maxRange.
        if (distanceSqr <= 7.0D * 7.0D) {

            attacks.add(
                    new AttackData(EARTH_QUAKE, 1.0D),
                    earthquakeWeight
            );
        }

        // Runic Blast is usable between 5 and 18 blocks.
        if (distanceSqr >= 25.0D && distanceSqr <= 324.0D) {

            attacks.add(
                    new AttackData(RUNIC_BLAST, 1.0D),
                    runicBlastWeight
            );
        }

        // Lock-on Beam charges for 60 ticks (3 seconds). Cover intercepts the
        // beam, and prevents damage if it remains in the way when it discharges.
        if (distanceSqr >= 36.0D && distanceSqr <= 625.0D && hasLineOfSight(target)) {
            attacks.add(
                    new AttackData(LOCK_ON_BEAM, 1.0D),
                    lockOnBeamWeight
            );
        }

        return attacks;
    }

    @Override
    public WeightedList<AttackData> getRageAttacks() {
        return WeightedList.empty();
    }

    @Override
    public double getAttackReach() {
        return 16.0D;
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
