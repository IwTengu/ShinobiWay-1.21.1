package net.iwtengu.shinobiway.renderer;

import net.iwtengu.shinobiway.ShinobiWay;
import net.iwtengu.shinobiway.client.ClientEyeData;
import net.iwtengu.shinobiway.entity.custom.SeatEntity;
import net.iwtengu.shinobiway.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(modid = "shinobiway", value = Dist.CLIENT)
public class EyeOverlayRenderer {

    private static final ResourceLocation BLACK =
            ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/overlay/black.png");

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!ClientEyeData.get(mc.player.getUUID())) return;

        if (mc.player.getVehicle() instanceof SeatEntity) return;

        boolean hasRightEye = hasEyeInSlot("right_eye");
        boolean hasLeftEye  = hasEyeInSlot("left_eye");

        if (hasRightEye && hasLeftEye) return;


        GuiGraphics gui  = event.getGuiGraphics();
        int screenWidth  = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int halfWidth    = screenWidth / 2;

        if (!hasRightEye) {
            gui.blit(
                    BLACK,
                    halfWidth, 0,
                    0, 0,
                    halfWidth, screenHeight,
                    1, 1
            );
        }

        if (!hasLeftEye) {
            gui.blit(
                    BLACK,
                    0, 0,
                    0, 0,
                    halfWidth, screenHeight,
                    1, 1
            );
        }
    }

    private static boolean hasEyeInSlot(String slotId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        return CuriosApi.getCuriosInventory(mc.player)
                .map(handler -> handler.getStacksHandler(slotId)
                        .map(stacksHandler -> {
                            for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                                if (!stack.isEmpty() && stack.is(ModItems.EYE.get())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .orElse(false)
                )
                .orElse(false);
    }
}