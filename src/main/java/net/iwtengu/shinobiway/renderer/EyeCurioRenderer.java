package net.iwtengu.shinobiway.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.iwtengu.shinobiway.ShinobiWay;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class EyeCurioRenderer implements ICurioRenderer {

    // -------------------------------------------------------------------------
    // ОСНОВАНИЯ ГЛАЗ
    // -------------------------------------------------------------------------
    private static final ResourceLocation RIGHT_EYE_BASE =
            ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/curio/right_eye_base.png");
    private static final ResourceLocation LEFT_EYE_BASE =
            ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/curio/left_eye_base.png");

    // -------------------------------------------------------------------------
    // ЗРАЧКИ
    // -------------------------------------------------------------------------
    private static final ResourceLocation RIGHT_EYE_PUPIL =
            ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/curio/right_eye_pupil.png");
    private static final ResourceLocation LEFT_EYE_PUPIL =
            ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/curio/left_eye_pupil.png");

    // -------------------------------------------------------------------------
    // НАСТРОЙКИ ОСНОВАНИЯ ГЛАЗА
    // -------------------------------------------------------------------------
    private static final float EYE_BASE_OFFSET_X       = 0.125f;
    private static final float EYE_BASE_OFFSET_Y       = -0.15f;
    private static final float EYE_BASE_OFFSET_Z       = 0.300f;
    private static final float EYE_BASE_SIZE           = 0.30f;
    private static final float EYE_BASE_ROTATION_Z_DEG = 0f;

    // -------------------------------------------------------------------------
    // НАСТРОЙКИ ЗРАЧКА
    // -------------------------------------------------------------------------
    private static final float EYE_PUPIL_OFFSET_X       = 0.125f;
    private static final float EYE_PUPIL_OFFSET_Y       = -0.15f;
    private static final float EYE_PUPIL_OFFSET_Z       = 0.300f;
    private static final float EYE_PUPIL_SIZE           = 0.30f;
    private static final float EYE_PUPIL_ROTATION_Z_DEG = -0.001f;

    // -------------------------------------------------------------------------

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> parent,
            MultiBufferSource multiBufferSource,
            int packedLight,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!(parent.getModel() instanceof HumanoidModel<?> humanoidModel)) return;

        boolean isRightEye = slotContext.identifier().equals("right_eye");
        boolean isLeftEye  = slotContext.identifier().equals("left_eye");

        if (!isRightEye && !isLeftEye) return;

        ResourceLocation baseTexture  = isRightEye ? RIGHT_EYE_BASE  : LEFT_EYE_BASE;
        ResourceLocation pupilTexture = isRightEye ? RIGHT_EYE_PUPIL : LEFT_EYE_PUPIL;

        float baseOffsetX  = isRightEye ? EYE_BASE_OFFSET_X  : -EYE_BASE_OFFSET_X;
        float pupilOffsetX = isRightEye ? EYE_PUPIL_OFFSET_X : -EYE_PUPIL_OFFSET_X;

        poseStack.pushPose();

        humanoidModel.head.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // Основание глаза
        poseStack.pushPose();
        poseStack.translate(baseOffsetX, EYE_BASE_OFFSET_Y, EYE_BASE_OFFSET_Z);

        if (EYE_BASE_ROTATION_Z_DEG != 0f) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(EYE_BASE_ROTATION_Z_DEG));
        }

        renderQuad(
                poseStack,
                multiBufferSource,
                packedLight,
                baseTexture,
                0f,
                EYE_BASE_SIZE
        );

        poseStack.popPose();

        // Зрачок
        poseStack.pushPose();
        poseStack.translate(pupilOffsetX, EYE_PUPIL_OFFSET_Y, EYE_PUPIL_OFFSET_Z);

        if (EYE_PUPIL_ROTATION_Z_DEG != 0f) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(EYE_PUPIL_ROTATION_Z_DEG));
        }

        renderQuad(
                poseStack,
                multiBufferSource,
                packedLight,
                pupilTexture,
                -0.001f,
                EYE_PUPIL_SIZE
        );

        poseStack.popPose();
        poseStack.popPose();
    }

    private static void renderQuad(
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int packedLight,
            ResourceLocation texture,
            float zOffset,
            float size
    ) {
        poseStack.pushPose();

        if (zOffset != 0f) {
            poseStack.translate(0, 0, -zOffset);
        }

        VertexConsumer consumer = multiBufferSource.getBuffer(
                RenderType.entityCutoutNoCull(texture)
        );

        var pose  = poseStack.last().pose();
        var entry = poseStack.last();
        float half = size / 2f;

        consumer.addVertex(pose, -half,  half, 0).setColor(255,255,255,255).setUv(0,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(entry,0,0,1);
        consumer.addVertex(pose,  half,  half, 0).setColor(255,255,255,255).setUv(1,1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(entry,0,0,1);
        consumer.addVertex(pose,  half, -half, 0).setColor(255,255,255,255).setUv(1,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(entry,0,0,1);
        consumer.addVertex(pose, -half, -half, 0).setColor(255,255,255,255).setUv(0,0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(entry,0,0,1);

        poseStack.popPose();
    }
}