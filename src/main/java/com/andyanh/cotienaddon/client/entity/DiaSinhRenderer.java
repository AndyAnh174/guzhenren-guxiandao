package com.andyanh.cotienaddon.client.entity;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.entity.DiaSinhEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class DiaSinhRenderer extends MobRenderer<DiaSinhEntity, PlayerModel<DiaSinhEntity>> {

    private static final ResourceLocation[] SKINS = new ResourceLocation[DiaSinhEntity.SKIN_COUNT];

    static {
        for (int i = 0; i < DiaSinhEntity.SKIN_COUNT; i++) {
            SKINS[i] = ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID,
                    "textures/entity/dia_linh/skin_" + i + ".png");
        }
    }

    public DiaSinhRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(DiaSinhEntity entity) {
        int idx = entity.getSkinIndex();
        return SKINS[Math.max(0, Math.min(idx, SKINS.length - 1))];
    }

    @Override
    protected void scale(DiaSinhEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.9375f, 0.9375f, 0.9375f);
    }
}
