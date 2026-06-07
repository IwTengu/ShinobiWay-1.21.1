package net.iwtengu.shinobiway;

import com.mojang.logging.LogUtils;
import net.iwtengu.shinobiway.animation.AnimationPacketHandler;
import net.iwtengu.shinobiway.animation.PlayerAnimationManager;
import net.iwtengu.shinobiway.animation.PlayerAnimationState;
import net.iwtengu.shinobiway.block.ModBlocks;
import net.iwtengu.shinobiway.chakra.ChakraAttachment;
import net.iwtengu.shinobiway.chakra.ChakraSyncPacket;
import net.iwtengu.shinobiway.client.ClientEyeData;
import net.iwtengu.shinobiway.combat.*;
import net.iwtengu.shinobiway.combat.client.animation.CombatWeaponRegistry;
import net.iwtengu.shinobiway.component.ModDataComponents;
import net.iwtengu.shinobiway.entity.ModEntities;
import net.iwtengu.shinobiway.event.*;
import net.iwtengu.shinobiway.item.ModCreativeTabs;
import net.iwtengu.shinobiway.item.ModItems;
import net.iwtengu.shinobiway.network.EyeUnlockPayload;
import net.iwtengu.shinobiway.network.MeditationSuccessPayload;
import net.iwtengu.shinobiway.player.PlayerEyeData;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import org.slf4j.Logger;

@Mod(ShinobiWay.MOD_ID)
public class ShinobiWay {

    public static final String MOD_ID = "shinobiway";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ShinobiWay(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);

        // ===== PAYLOADS =====
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {

            var registrar = event.registrar("1");

            registrar.playToClient(
                    EyeUnlockPayload.TYPE,
                    EyeUnlockPayload.CODEC,
                    (payload, context) -> context.enqueueWork(() ->
                            ClientEyeData.set(
                                    context.player().getUUID(),
                                    payload.unlocked()
                            )
                    )
            );

            registrar.playToServer(
                    MeditationSuccessPayload.TYPE,
                    MeditationSuccessPayload.CODEC,
                    (payload, context) -> context.enqueueWork(() -> {

                        if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {

                            net.iwtengu.shinobiway.chakra.ChakraData chakra =
                                    ChakraAttachment.get(player);

                            chakra.addMax(1f);
                            ChakraSyncPacket.send(player);
                        }
                    })
            );
        });

        NeoForge.EVENT_BUS.register(this);

        // ===== REGISTRIES =====
        ModCreativeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        ModDataComponents.register(modEventBus);

        // ===== CHAKRA =====
        ChakraAttachment.register(modEventBus);
        modEventBus.addListener(ChakraSyncPacket::register);

        PlayerEyeData.ATTACHMENT_TYPES.register(modEventBus);

        // ===== ANIMATION =====
        PlayerAnimationManager.registerAll();

        modEventBus.addListener((FMLClientSetupEvent e) ->
                PlayerAnimationState.registerFactories()
        );

        NeoForge.EVENT_BUS.register(PlayerAnimationManager.class);

        modEventBus.addListener(AnimationPacketHandler::registerPackets);

        // ===== EVENTS =====
        CommandRegister.register();
        PlayerCloneHandler.register();
        TargetHitEventHandler.register();

        // ===== COMBAT =====
        CombatAttachments.ATTACHMENT_TYPES.register(modEventBus);

        modEventBus.addListener(CombatNetwork::register);

        modEventBus.addListener((RegisterKeyMappingsEvent event) ->
                CombatKeyBindings.onRegisterKeyMappings(event)
        );

        modEventBus.addListener((FMLCommonSetupEvent e) -> {

            // группы оружия
            CombatWeaponRegistry.registerAll();

            // скорости атак
            CombatAttackRegistry.registerAll();
        });

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                Config.SPEC
        );
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(
            modid = ShinobiWay.MOD_ID,
            value = Dist.CLIENT
    )
    public static class ClientModEvents {

        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}