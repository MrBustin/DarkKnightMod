package com.bustin.knightmod.client;

import com.bustin.knightmod.KnightMod;
import com.bustin.knightmod.client.renderer.DarkKnightRenderer;
import com.bustin.knightmod.client.renderer.TestEntityRenderer;
import com.bustin.knightmod.entity.projectile.RunicBlastProjectile;
import com.bustin.knightmod.init.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = KnightMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.TEST_ENTITY.get(), TestEntityRenderer::new);
            EntityRenderers.register(ModEntities.DARK_KNIGHT.get(), DarkKnightRenderer::new);
            EntityRenderers.register(ModEntities.RUNIC_BLAST.get(), ThrownItemRenderer::new);
        });
    }
}
