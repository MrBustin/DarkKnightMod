package com.bustin.knightmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * Basic test mob. It intentionally uses vanilla zombie behavior and visuals.
 */
public class TestEntity extends Zombie {
    public TestEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }
}
