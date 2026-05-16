package net.iwtengu.shinobiway.entity.custom;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class ShurikenRenderer extends EntityRenderer<ShurikenEntity> {

    private final ItemRenderer itemRenderer;

    public ShurikenRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ShurikenEntity entity,
                       float entityYaw,
                       float partialTicks,
                       PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight) {

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));

        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        if (!entity.isInGround()) {
            float spin = (entity.tickCount + partialTicks) * 40F;
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        }

        ItemStack stack = entity.getShurikenItem();

        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(ShurikenEntity entity) {
        return null;
    }
}
