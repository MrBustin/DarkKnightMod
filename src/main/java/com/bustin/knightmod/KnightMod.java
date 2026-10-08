package com.bustin.knightmod;

import com.mojang.logging.LogUtils;
import com.bustin.knightmod.init.ModBlocks;
import com.bustin.knightmod.init.ModEntities;
import com.bustin.knightmod.init.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(KnightMod.MOD_ID)
public class KnightMod {
    public static final String MOD_ID = "knightmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public KnightMod() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Knight Mod initialized");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Knight Mod server setup complete");
    }
}
