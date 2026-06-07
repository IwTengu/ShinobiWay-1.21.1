package net.iwtengu.shinobiway.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.iwtengu.shinobiway.ShinobiWay;
import net.iwtengu.shinobiway.client.ClientEyeData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class EyeEmptyLayerRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation EMPTY_RIGHT_EYE =
            ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/curio/empty_right_eye.png");
    private static final ResourceLocation EMPTY_LEFT_EYE =
            ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/curio/empty_left_eye.png");

    // Те же значения что и в EyeCurioRenderer
    private static final float OFFSET_X       = 0.125f;
    private static final float OFFSET_Y       = -0.15f;
    private static final float OFFSET_Z       = 0.300f;
    private static final float SIZE           = 0.30f;
    private static final float ROTATION_Z_DEG = 0f;

    public EyeEmptyLayerRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        // Проверяем клиентский кэш
        if (!ClientEyeData.get(player.getUUID())) {
            return;
        }

        boolean rightEmpty = isSlotUnlockedAndEmpty(player, "right_eye");
        boolean leftEmpty  = isSlotUnlockedAndEmpty(player, "left_eye");

        if (!rightEmpty && !leftEmpty) {
            return;
        }

        HumanoidModel<AbstractClientPlayer> model = this.getParentModel();

        if (rightEmpty) {
            renderEye(poseStack, multiBufferSource, packedLight, model, EMPTY_RIGHT_EYE, OFFSET_X);
        }
        if (leftEmpty) {
            renderEye(poseStack, multiBufferSource, packedLight, model, EMPTY_LEFT_EYE, -OFFSET_X);
        }
    }

    // Точная копия метода из EyeCurioRenderer
    private static void renderEye(
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int packedLight,
            HumanoidModel<?> humanoidModel,
            ResourceLocation texture,
            float offsetX
    ) {
        poseStack.pushPose();

        humanoidModel.head.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.translate(offsetX, OFFSET_Y, OFFSET_Z);

        if (ROTATION_Z_DEG != 0f) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(ROTATION_Z_DEG));
        }

        VertexConsumer consumer = multiBufferSource.getBuffer(
                RenderType.entityCutoutNoCull(texture)
        );

        var pose  = poseStack.last().pose();
        var entry = poseStack.last();
        float half = SIZE / 2f;

        consumer.addVertex(pose, -half,  half, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(entry, 0, 0, 1);
        consumer.addVertex(pose,  half,  half, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(entry, 0, 0, 1);
        consumer.addVertex(pose,  half, -half, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(entry, 0, 0, 1);
        consumer.addVertex(pose, -half, -half, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(entry, 0, 0, 1);

        poseStack.popPose();
    }

    private static boolean isSlotUnlockedAndEmpty(AbstractClientPlayer player, String slotId) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.getStacksHandler(slotId)
                        .map(stacksHandler -> {
                            if (stacksHandler.getStacks().getSlots() == 0) {
                                return false;
                            }
                            for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                                if (!stack.isEmpty()) {
                                    return false;
                                }
                            }
                            return true;
                        })
                        .orElse(false)
                )
                .orElse(false);
    }
}