package com.bustin.knightmod.entity.dark_knight.attacks;

import iskallia.vault.entity.boss.VaultBossBaseEntity;
import iskallia.vault.entity.boss.attack.IMeleeAttack;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;
import java.util.Optional;

/**
 * Adapts an ability-like action to the Vault boss melee-attack state machine.
 *
 * <p>Vault Hunters player abilities require a {@code ServerPlayer}, so they cannot be invoked
 * directly by a mob. {@link BossAbility} is the boss-safe adapter point for reproducing an
 * ability's effect while retaining the boss as its caster and damage source.</p>
 */
public class BasicAbilityAttack implements IMeleeAttack {
    private final VaultBossBaseEntity boss;
    private final double damageMultiplier;
    private final BasicAbilityAttackAttributes attributes;
    private final BossAbility ability;

    private int ticks;
    private int targetId = -1;
    private boolean cast;

    public BasicAbilityAttack(VaultBossBaseEntity boss, double damageMultiplier, BasicAbilityAttackAttributes attributes, BossAbility ability) {
        this.boss = Objects.requireNonNull(boss, "boss");
        this.damageMultiplier = damageMultiplier;
        this.attributes = Objects.requireNonNull(attributes, "attributes");
        this.ability = Objects.requireNonNull(ability, "ability");
    }

    @Override
    public boolean start(LivingEntity target, double distToTarget, double reach) {
        if (targetId != -1 || target == null || !target.isAlive()) {
            return false;
        }

        // BossMeleeAttackGoal supplies squared distance here.
        double minRangeSqr = attributes.minRange() * attributes.minRange();
        double maxRangeSqr = attributes.maxRange() * attributes.maxRange();
        if (distToTarget < minRangeSqr || distToTarget > maxRangeSqr) {
            return false;
        }

        targetId = target.getId();
        ticks = 0;
        cast = false;
        return true;
    }

    @Override
    public void tick(double reach) {
        ticks++;

        if (!cast && ticks >= attributes.castTick()) {
            cast = true;
            castAbility();
        }
    }

    private void castAbility() {
        if (boss.level.isClientSide) {
            return;
        }

        if (!(boss.level.getEntity(targetId) instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }

        float damage = (float) (attributes.baseDamage() * damageMultiplier);
        ability.cast(boss, target, damage);
    }

    @Override
    public void stop() {
        targetId = -1;
        ticks = 0;
        cast = false;
    }

    @Override
    public int getDuration() {
        return attributes.duration();
    }

    @Override
    public Optional<String> getAttackMove() {
        return Optional.ofNullable(attributes.attackMoveName());
    }

    @FunctionalInterface
    public interface BossAbility {
        /**
         * Executes the ability on the logical server.
         *
         * @param caster boss casting the ability
         * @param target target captured when this attack started
         * @param damage configured base damage multiplied by the attack's difficulty multiplier
         */
        void cast(VaultBossBaseEntity caster, LivingEntity target, float damage);
    }

    public record BasicAbilityAttackAttributes(int duration, int castTick, String attackMoveName, float baseDamage, float minRange, float maxRange) {
        public BasicAbilityAttackAttributes {
            if (duration <= 0) {
                throw new IllegalArgumentException("duration must be greater than zero");
            }
            if (castTick < 0 || castTick > duration) {
                throw new IllegalArgumentException("castTick must be between zero and duration");
            }
            if (baseDamage < 0.0F) {
                throw new IllegalArgumentException("baseDamage cannot be negative");
            }
            if (minRange < 0.0F || maxRange < minRange) {
                throw new IllegalArgumentException("range must satisfy 0 <= minRange <= maxRange");
            }
        }
    }
}
