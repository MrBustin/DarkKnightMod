package com.bustin.knightmod.entity.dark_knight.attacks;

import iskallia.vault.entity.boss.VaultBossBaseEntity;
import iskallia.vault.entity.boss.attack.IMeleeAttack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

/**
 * A telegraphed, target-tracking beam. Cover intercepts the visual beam and
 * prevents damage if it is still blocking the beam when the charge completes.
 */
public class BeamAttack implements IMeleeAttack {
    private final VaultBossBaseEntity boss;
    private final double damageMultiplier;
    private final BeamAttackAttributes attributes;

    private int ticks;
    private int targetId = -1;
    private boolean fired;

    public BeamAttack(VaultBossBaseEntity boss, double damageMultiplier, BeamAttackAttributes attributes) {
        this.boss = Objects.requireNonNull(boss, "boss");
        this.damageMultiplier = damageMultiplier;
        this.attributes = Objects.requireNonNull(attributes, "attributes");
    }

    @Override
    public boolean start(LivingEntity target, double distToTarget, double reach) {
        if (targetId != -1 || target == null || !target.isAlive() || !boss.hasLineOfSight(target)) {
            return false;
        }

        double minRangeSqr = attributes.minRange() * attributes.minRange();
        double maxRangeSqr = attributes.maxRange() * attributes.maxRange();
        if (distToTarget < minRangeSqr || distToTarget > maxRangeSqr) {
            return false;
        }

        targetId = target.getId();
        ticks = 0;
        fired = false;
        return true;
    }

    @Override
    public void tick(double reach) {
        ticks++;

        if (fired || !(boss.level.getEntity(targetId) instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }

        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        drawBeam(target);

        if (ticks >= attributes.chargeTicks()) {
            fired = true;
            if (!boss.level.isClientSide) {
                // Cover only needs to be held until this discharge. The attack still
                // finishes and makes its sound, but never damages or destroys blocks.
                if (!isBeamBlocked(target)) {
                    target.hurt(DamageSource.mobAttack(boss), (float) (attributes.baseDamage() * damageMultiplier));
                }
                boss.level.playSound(null, boss.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                        SoundSource.HOSTILE, 0.8F, 1.4F);
            }
        }
    }

    private void drawBeam(LivingEntity target) {
        if (!(boss.level instanceof ServerLevel level)) {
            return;
        }

        Vec3 start = boss.position().add(0.0D, boss.getBbHeight() * attributes.originHeight(), 0.0D);
        Vec3 end = getBeamEnd(target, start);
        Vec3 beam = end.subtract(start);
        double length = beam.length();
        if (length < 1.0E-4D) {
            return;
        }

        Vec3 step = beam.normalize().scale(attributes.particleSpacing());
        int points = (int) Math.floor(length / attributes.particleSpacing());
        for (int i = 0; i <= points; i++) {
            Vec3 point = start.add(step.scale(i));
            level.sendParticles(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z,
                    1, 0.015D, 0.015D, 0.015D, 0.0D);
        }

        // A brighter endpoint makes the locked target and charge-up easy to read.
        if (ticks % 3 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, end.x, end.y, end.z,
                    1, 0.08D, 0.08D, 0.08D, 0.0D);
        }
    }

    private boolean isBeamBlocked(LivingEntity target) {
        Vec3 start = boss.position().add(0.0D, boss.getBbHeight() * attributes.originHeight(), 0.0D);
        return getBlockHit(target, start).getType() == HitResult.Type.BLOCK;
    }

    private Vec3 getBeamEnd(LivingEntity target, Vec3 start) {
        BlockHitResult hit = getBlockHit(target, start);
        return hit.getType() == HitResult.Type.BLOCK
                ? hit.getLocation()
                : target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
    }

    private BlockHitResult getBlockHit(LivingEntity target, Vec3 start) {
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        return boss.level.clip(new ClipContext(
                start,
                targetCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                boss
        ));
    }

    @Override
    public void stop() {
        targetId = -1;
        ticks = 0;
        fired = false;
    }

    @Override
    public int getDuration() {
        return attributes.duration();
    }

    @Override
    public Optional<String> getAttackMove() {
        return Optional.ofNullable(attributes.attackMoveName());
    }

    public record BeamAttackAttributes(int duration, int chargeTicks, String attackMoveName, float baseDamage, float minRange, float maxRange, float originHeight, float particleSpacing) {
        public BeamAttackAttributes {
            if (duration <= 0) throw new IllegalArgumentException("duration must be greater than zero");
            if (chargeTicks < 1 || chargeTicks > duration) throw new IllegalArgumentException("chargeTicks must be between 1 and duration");
            if (baseDamage < 0.0F) throw new IllegalArgumentException("baseDamage cannot be negative");
            if (minRange < 0.0F || maxRange < minRange) throw new IllegalArgumentException("range must satisfy 0 <= minRange <= maxRange");
            if (originHeight < 0.0F || originHeight > 1.0F) throw new IllegalArgumentException("originHeight must be between 0 and 1");
            if (particleSpacing <= 0.0F) throw new IllegalArgumentException("particleSpacing must be greater than zero");
        }
    }
}
