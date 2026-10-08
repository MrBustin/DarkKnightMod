package com.bustin.knightmod.client.renderer;

import com.bustin.knightmod.entity.dark_knight.DarkKnightEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Temporary model mapping that gives the Dark Knight Vault Hunters' Naga appearance.
 */
public class DarkKnightModel extends AnimatedGeoModel<DarkKnightEntity> {
    private static final ResourceLocation MODEL =
            new ResourceLocation("the_vault", "geo/champion.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("the_vault", "textures/entity/champion.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation("the_vault", "animations/champion.animation.json");

    @Override
    public ResourceLocation getModelLocation(DarkKnightEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(DarkKnightEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(DarkKnightEntity entity) {
        return ANIMATION;
    }
}
