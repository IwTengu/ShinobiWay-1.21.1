package net.iwtengu.shinobiway.entity.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SenbonRenderer extends EntityRenderer<SenbonEntity> {

    private final ItemRenderer itemRenderer;

    public SenbonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(SenbonEntity entity,
                       float entityYaw,
                       float partialTicks,
                       PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight) {

        poseStack.pushPose();

        float yRot = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks;
        float xRot = entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTicks;

        poseStack.mulPose(Axis.YP.rotationDegrees(yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(xRot - 90.0F));

        poseStack.translate(0.0D, -0.15D, 0.0D);

        ItemStack stack = entity.getSenbonItem();

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
    public net.minecraft.resources.ResourceLocation getTextureLocation(SenbonEntity entity) {
        return null;
    }
}