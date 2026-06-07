package net.iwtengu.shinobiway.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.iwtengu.shinobiway.client.HeadbandModel;
import net.iwtengu.shinobiway.item.custom.HeadbandItem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class HeadbandRenderer extends GeoArmorRenderer<HeadbandItem>
        implements ICurioRenderer {

    public HeadbandRenderer() {
        super(new HeadbandModel());
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource bufferSource,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel)) return;

        // Используем актуальный prepForRender (не deprecated)
        prepForRender(
                slotContext.entity(),
                stack,
                EquipmentSlot.HEAD,
                humanoidModel,
                bufferSource,
                partialTicks,
                limbSwing,
                limbSwingAmount,
                netHeadYaw,
                headPitch
        );

        // GeoArmorRenderer.renderToBuffer делает всё сам
        this.renderToBuffer(poseStack, null, light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, -1);
    }
}