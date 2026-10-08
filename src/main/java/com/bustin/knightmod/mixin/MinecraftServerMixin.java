package com.bustin.knightmod.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Marker mixin that verifies the Mixin toolchain is active without changing behavior.
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
}
