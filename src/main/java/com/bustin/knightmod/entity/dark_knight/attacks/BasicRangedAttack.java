package com.bustin.knightmod.entity.dark_knight.attacks;

import iskallia.vault.entity.boss.VaultBossBaseEntity;
import iskallia.vault.entity.boss.attack.IMeleeAttack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.BiFunction;

public class BasicRangedAttack implements IMeleeAttack {

    private final VaultBossBaseEntity boss;
    private final double damageMultiplier;
    private final BasicRangedAttackAttributes attributes;
    private final BiFunction<VaultBossBaseEntity, Level, ? extends Projectile> projectileFactory;

    private int ticks;
    private int targetId = -1;
    private boolean fired;

    public BasicRangedAttack(VaultBossBaseEntity boss, double damageMultiplier, BasicRangedAttackAttributes attributes, BiFunction<VaultBossBaseEntity, Level, ? extends Projectile> projectileFactory) {
        this.boss = boss;
        this.damageMultiplier = damageMultiplier;
        this.attributes = attributes;
        this.projectileFactory = projectileFactory;
    }

    @Override
    public boolean start(LivingEntity target, double distToTarget, double reach) {
        // BossMeleeAttackGoal passes squared distance.
        if (distToTarget < attributes.minRange() *
                attributes.minRange()
                || distToTarget > attributes.maxRange() *
                attributes.maxRange()) {
            return false;
        }

        this.targetId = target.getId();
        this.ticks = 0;
        this.fired = false;

        return true;
    }

    @Override
    public void tick(double reach) {
        ticks++;

        if (!fired && ticks >= attributes.fireTick()) {
            fired = true;
            fireProjectile();
        }
    }

    private void fireProjectile() {
        if (boss.level.isClientSide) {
            return;
        }

        // Get the player's CURRENT position at firing time.
        if (!(boss.level.getEntity(targetId) instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }

        Projectile projectile = projectileFactory.apply(boss, boss.level);

        if (projectile == null) {
            return;
        }

        projectile.setOwner(boss);

        Vec3 origin = boss.position().add(0.0, boss.getBbHeight() * attributes.spawnHeight(), 0.0);

        Vec3 targetPosition = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);

        Vec3 direction = targetPosition.subtract(origin).normalize();

        projectile.setPos(origin.x, origin.y, origin.z);

        projectile.shoot(direction.x, direction.y, direction.z, attributes.projectileSpeed(), attributes.inaccuracy());

        boss.level.addFreshEntity(projectile);
    }

    @Override
    public void stop() {
        targetId = -1;
        fired = false;
    }

    @Override
    public int getDuration() {
        return attributes.duration();
    }

    @Override
    public Optional<String> getAttackMove() {
        return Optional.of(attributes.attackMoveName());
    }

    public record BasicRangedAttackAttributes(
            int duration,
            int fireTick,
            String attackMoveName,
            float projectileSpeed,
            float inaccuracy,
            float minRange,
            float maxRange,
            float spawnHeight
    ) {}
}
