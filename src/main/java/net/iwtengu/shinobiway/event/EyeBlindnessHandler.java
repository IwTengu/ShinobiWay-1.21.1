package net.iwtengu.shinobiway.event;

import net.iwtengu.shinobiway.item.ModItems;
import net.iwtengu.shinobiway.player.PlayerEyeData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@EventBusSubscriber(modid = "shinobiway")
public class EyeBlindnessHandler {

    private static final int CHECK_INTERVAL = 40;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_INTERVAL != 0) return;
        if (!player.getData(PlayerEyeData.EYE_SLOTS_UNLOCKED)) return;

        // Визуальный эффект теперь полностью на клиенте через EyeOverlayRenderer
        // Здесь можно добавить серверную логику если понадобится
    }

    public static boolean hasEyeInSlot(ICuriosItemHandler handler, String slotId) {
        return handler.getStacksHandler(slotId)
                .map(stacksHandler -> {
                    for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty() && stack.is(ModItems.EYE.get())) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }
}