package com.bustin.knightmod.init;

import com.bustin.knightmod.KnightMod;
import com.bustin.knightmod.entity.TestEntity;
import com.bustin.knightmod.entity.dark_knight.DarkKnightEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, KnightMod.MOD_ID);

    public static final RegistryObject<EntityType<TestEntity>> TEST_ENTITY = ENTITIES.register("test_entity",
            () -> EntityType.Builder.of(TestEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(new ResourceLocation(KnightMod.MOD_ID, "test_entity").toString()));

    public static final RegistryObject<EntityType<DarkKnightEntity>> DARK_KNIGHT = ENTITIES.register("dark_knight",
            () -> EntityType.Builder.of(DarkKnightEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.5F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(KnightMod.MOD_ID, "dark_knight").toString()));

    private ModEntities() {
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
        eventBus.addListener(ModEntities::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TEST_ENTITY.get(), TestEntity.createAttributes().build());
        event.put(DARK_KNIGHT.get(), DarkKnightEntity.createAttributes().build());
    }
}
