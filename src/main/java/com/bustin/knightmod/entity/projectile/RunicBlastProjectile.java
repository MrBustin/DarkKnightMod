package com.bustin.knightmod.entity.projectile;

import com.bustin.knightmod.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class RunicBlastProjectile extends ThrowableItemProjectile {
    private static final float DAMAGE = 12.0F;
    private static final int MAX_LIFETIME = 40;

    public RunicBlastProjectile(EntityType<? extends RunicBlastProjectile> type, Level level) {
        super(type, level);
    }

    public RunicBlastProjectile(Level level, LivingEntity owner) {
        this(ModEntities.RUNIC_BLAST.get(), level);
        this.setOwner(owner);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AMETHYST_SHARD;
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide) {
            level.addParticle(ParticleTypes.WITCH, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
        }

        if (!level.isClientSide && tickCount >= MAX_LIFETIME) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (level.isClientSide) {
            return;
        }

        Entity target = result.getEntity();
        Entity owner = getOwner();

        if (target == owner) {
            return;
        }

        DamageSource source = owner instanceof LivingEntity living
                ? DamageSource.mobAttack(living)
                : DamageSource.MAGIC;

        target.hurt(source, DAMAGE);
        discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!level.isClientSide) {
            discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return (Packet<ClientGamePacketListener>) NetworkHooks.getEntitySpawningPacket(this);
    }
}
