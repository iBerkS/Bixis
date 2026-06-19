package com.bixis.bixismod.client;

import com.bixis.bixismod.Constants;
import com.bixis.bixismod.mob.AbugatEntity;
import com.bixis.bixismod.mob.GeorgeFloidEntity;
import com.bixis.bixismod.mob.HoliganEntity;
import com.bixis.bixismod.mob.TurkPolisiEntity;
import com.bixis.bixismod.mob.KemalDarkilicogluEntity;
import com.bixis.bixismod.mob.RecepIvediEntity;
import com.bixis.bixismod.npc.BixisEntities;
import com.bixis.bixismod.npc.RahimKocEntity;
import com.bixis.bixismod.npc.VillaHakanEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import com.bixis.bixismod.weapon.BulletProjectileEntity;
import com.bixis.bixismod.weapon.MizrakProjectileEntity;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-side renderer kayıtları. Sadece Dist.CLIENT'ta çalışır. */
@Mod.EventBusSubscriber(modid = "bixis", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BixisClientSetup {

    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BixisEntities.RAHIM_KOC.get(), ctx ->
            new HumanoidMobRenderer<RahimKocEntity, HumanoidModel<RahimKocEntity>>(
                ctx,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)),
                Constants.NPC_SHADOW_RADIUS
            ) {
                private static final ResourceLocation TEX =
                    new ResourceLocation("bixis:textures/entity/rahim_koc.png");

                @Override
                public ResourceLocation getTextureLocation(RahimKocEntity entity) { return TEX; }
            }
        );

        event.registerEntityRenderer(BixisEntities.VILLA_HAKAN.get(), ctx ->
            new HumanoidMobRenderer<VillaHakanEntity, HumanoidModel<VillaHakanEntity>>(
                ctx,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)),
                Constants.NPC_SHADOW_RADIUS
            ) {
                private static final ResourceLocation TEX =
                    new ResourceLocation("bixis:textures/entity/villa_hakan.png");

                @Override
                public ResourceLocation getTextureLocation(VillaHakanEntity entity) { return TEX; }
            }
        );

        event.registerEntityRenderer(BixisEntities.HIRT.get(), HirtRenderer::new);

        event.registerEntityRenderer(BixisEntities.RECEP_IVEDI.get(), ctx ->
            new HumanoidMobRenderer<RecepIvediEntity, HumanoidModel<RecepIvediEntity>>(
                ctx,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)),
                Constants.NPC_SHADOW_RADIUS
            ) {
                private static final ResourceLocation TEX =
                    new ResourceLocation("bixis:textures/entity/recep_ivedi.png");

                @Override
                public ResourceLocation getTextureLocation(RecepIvediEntity entity) { return TEX; }
            }
        );

        event.registerEntityRenderer(BixisEntities.KEMAL_DARKILICOGLU.get(), ctx ->
            new HumanoidMobRenderer<KemalDarkilicogluEntity, HumanoidModel<KemalDarkilicogluEntity>>(
                ctx,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)),
                Constants.NPC_SHADOW_RADIUS
            ) {
                private static final ResourceLocation TEX =
                    new ResourceLocation("bixis:textures/entity/kemal_darkilicoglu.png");

                @Override
                public ResourceLocation getTextureLocation(KemalDarkilicogluEntity entity) { return TEX; }
            }
        );

        event.registerEntityRenderer(BixisEntities.GEORGE_FLOID.get(), ctx ->
            new HumanoidMobRenderer<GeorgeFloidEntity, HumanoidModel<GeorgeFloidEntity>>(
                ctx,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)),
                Constants.NPC_SHADOW_RADIUS
            ) {
                private static final ResourceLocation TEX =
                    new ResourceLocation("bixis:textures/entity/george_floid.png");

                @Override
                public ResourceLocation getTextureLocation(GeorgeFloidEntity entity) { return TEX; }
            }
        );

        event.registerEntityRenderer(BixisEntities.ABUGAT.get(), ctx ->
            new HumanoidMobRenderer<AbugatEntity, HumanoidModel<AbugatEntity>>(
                ctx,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)),
                Constants.NPC_SHADOW_RADIUS
            ) {
                private static final ResourceLocation TEX =
                    new ResourceLocation("bixis:textures/entity/abugat.png");

                @Override
                public ResourceLocation getTextureLocation(AbugatEntity entity) { return TEX; }
            }
        );

        event.registerEntityRenderer(BixisEntities.HOLIGAN_FENERBAHCE.get(), ctx ->
            new HoliganRenderer(ctx, new ResourceLocation("bixis:textures/entity/holigan_fenerbahce.png")));

        event.registerEntityRenderer(BixisEntities.HOLIGAN_GALATASARAY.get(), ctx ->
            new HoliganRenderer(ctx, new ResourceLocation("bixis:textures/entity/holigan_galatasaray.png")));

        event.registerEntityRenderer(BixisEntities.HOLIGAN_BESIKTAS.get(), ctx ->
            new HoliganRenderer(ctx, new ResourceLocation("bixis:textures/entity/holigan_besiktas.png")));

        event.registerEntityRenderer(BixisEntities.HOLIGAN_TRABZONSPOR.get(), ctx ->
            new HoliganRenderer(ctx, new ResourceLocation("bixis:textures/entity/holigan_trabzonspor.png")));

        event.registerEntityRenderer(BixisEntities.TURK_POLISI.get(), ctx -> {
            HumanoidModel<TurkPolisiEntity> model =
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE));
            HumanoidMobRenderer<TurkPolisiEntity, HumanoidModel<TurkPolisiEntity>> renderer =
                new HumanoidMobRenderer<>(ctx, model, Constants.NPC_SHADOW_RADIUS) {
                    private static final ResourceLocation TEX =
                        new ResourceLocation("bixis:textures/entity/turk_polisi.png");
                    @Override
                    public ResourceLocation getTextureLocation(TurkPolisiEntity entity) { return TEX; }
                };
            renderer.addLayer(new ItemInHandLayer<>(renderer, ctx.getItemInHandRenderer()));
            return renderer;
        });

        // Mermi — ThrownItemRenderer, bixis:mermi item modelini render eder
        event.registerEntityRenderer(BixisEntities.BULLET.get(),
            ThrownItemRenderer::new);

        // Mızrak — ThrownItemRenderer, getItem() çağrısıyla mızrak item modelini projectile olarak render eder
        event.registerEntityRenderer(BixisEntities.MIZRAK_PROJECTILE.get(),
            ThrownItemRenderer::new);
    }

    private BixisClientSetup() {}
}
