package net.iwtengu.shinobiway.client;

import net.iwtengu.shinobiway.entity.ModEntities;
import net.iwtengu.shinobiway.entity.custom.KunaiRenderer;
import net.iwtengu.shinobiway.entity.custom.SeatRenderer;
import net.iwtengu.shinobiway.entity.custom.SenbonRenderer;
import net.iwtengu.shinobiway.entity.custom.ShurikenRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {

        event.enqueueWork(() -> {

            EntityRenderers.register(
                    ModEntities.SHURIKEN.get(),
                    ShurikenRenderer::new
            );

            EntityRenderers.register(
                    ModEntities.KUNAI.get(),
                    KunaiRenderer::new
            );

            EntityRenderers.register(
                    ModEntities.SENBON.get(),
                    SenbonRenderer::new
            );

            EntityRenderers.register(
                    ModEntities.SEAT.get(),
                    SeatRenderer::new
            );

        });
    }
}