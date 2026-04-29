package com.andyanh.cotienaddon.client.entity;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.entity.ThachNhanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ThachNhanRenderer extends MobRenderer<ThachNhanEntity, PlayerModel<ThachNhanEntity>> {

    private static final ResourceLocation[] SKINS = new ResourceLocation[5];

    static {
        for (int i = 0; i < 5; i++) {
            SKINS[i] = ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID,
                    "textures/entity/thach_nhan/skin_" + (i + 1) + ".png");
        }
    }

    public ThachNhanRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(ThachNhanEntity entity) {
        int idx = Math.max(0, Math.min(entity.getSkinLevel() - 1, 4));
        return SKINS[idx];
    }

    @Override
    protected void scale(ThachNhanEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.0f, 1.0f, 1.0f);
    }
}
