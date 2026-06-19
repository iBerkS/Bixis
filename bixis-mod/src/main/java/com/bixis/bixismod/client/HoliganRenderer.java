package com.bixis.bixismod.client;

import com.bixis.bixismod.Constants;
import com.bixis.bixismod.mob.HoliganEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Tüm holigan tipleri için paylaşılan renderer. Texture constructor'da verilir. */
public class HoliganRenderer extends HumanoidMobRenderer<HoliganEntity, HumanoidModel<HoliganEntity>> {

    private final ResourceLocation texture;

    public HoliganRenderer(EntityRendererProvider.Context ctx, ResourceLocation texture) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), Constants.NPC_SHADOW_RADIUS);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(HoliganEntity entity) { return texture; }
}
