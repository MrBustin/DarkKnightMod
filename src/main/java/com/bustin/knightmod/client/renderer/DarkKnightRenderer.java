package com.bustin.knightmod.client.renderer;

import com.bustin.knightmod.entity.dark_knight.DarkKnightEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class DarkKnightRenderer extends GeoEntityRenderer<DarkKnightEntity> {
    public DarkKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new DarkKnightModel());
        this.shadowRadius = 1.0F;
    }
}
