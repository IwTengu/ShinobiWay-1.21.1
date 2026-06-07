package net.iwtengu.shinobiway.client;

import net.iwtengu.shinobiway.entity.ModEntities;
import net.iwtengu.shinobiway.entity.custom.KunaiRenderer;
import net.iwtengu.shinobiway.entity.custom.SeatRenderer;
import net.iwtengu.shinobiway.entity.custom.SenbonRenderer;
import net.iwtengu.shinobiway.entity.custom.ShurikenRenderer;
import net.iwtengu.shinobiway.item.ModItems;
import net.iwtengu.shinobiway.renderer.EyeCurioRenderer;
import net.iwtengu.shinobiway.renderer.EyeEmptyLayerRenderer;
import net.iwtengu.shinobiway.renderer.HeadbandRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = "shinobiway", value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.SHURIKEN.get(), ShurikenRenderer::new);
            EntityRenderers.register(ModEntities.KUNAI.get(),    KunaiRenderer::new);
            EntityRenderers.register(ModEntities.SENBON.get(),   SenbonRenderer::new);
            EntityRenderers.register(ModEntities.SEAT.get(),     SeatRenderer::new);

            CuriosRendererRegistry.register(ModItems.EYE.get(), EyeCurioRenderer::new);
            CuriosRendererRegistry.register(ModItems.HEADBAND_LEAF.get(), HeadbandRenderer::new);
            CuriosRendererRegistry.register(ModItems.HEADBAND_SAND.get(), HeadbandRenderer::new);
            CuriosRendererRegistry.register(ModItems.HEADBAND_LEAF_BROKEN.get(), HeadbandRenderer::new);
            CuriosRendererRegistry.register(ModItems.HEADBAND_SAND_BROKEN.get(), HeadbandRenderer::new);
        });
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : PlayerSkin.Model.values()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new EyeEmptyLayerRenderer(playerRenderer));
            }
        }
    }
}