package com.bixis.bixismod.client;

import com.bixis.bixismod.mob.HirtEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * HirtEntity renderer — HirtVariant (0-3) değerine göre doğru texture'ı seçer.
 * hirt_1.png (gri), hirt_2.png (mor), hirt_3.png (kırmızı), hirt_4.png (yeşil).
 */
public class HirtRenderer extends HumanoidMobRenderer<HirtEntity, HumanoidModel<HirtEntity>> {

    private static final ResourceLocation[] TEXTURES = {
        new ResourceLocation("bixis", "textures/entity/hirt_1.png"),
        new ResourceLocation("bixis", "textures/entity/hirt_2.png"),
        new ResourceLocation("bixis", "textures/entity/hirt_3.png"),
        new ResourceLocation("bixis", "textures/entity/hirt_4.png"),
    };

    public HirtRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(HirtEntity entity) {
        int v = entity.getVariant();
        return (v >= 0 && v < TEXTURES.length) ? TEXTURES[v] : TEXTURES[0];
    }
}
