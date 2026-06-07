package net.iwtengu.shinobiway.item.custom;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.iwtengu.shinobiway.renderer.HeadbandRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class HeadbandItem extends Item implements ICurioItem, GeoItem {

    private final ResourceLocation texture;
    private final double armor;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * @param texture путь к текстуре повязки
     * @param armor   количество единиц защиты (1.0 = полкуска, 2.0 = 1 кусок на панели)
     */
    public HeadbandItem(ResourceLocation texture, double armor) {
        super(new Item.Properties().stacksTo(1));
        // Если захочешь прочность — замени строку выше на:
        this.texture = texture;
        this.armor = armor;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public ResourceLocation getHeadbandTexture() {
        return texture;
    }

    // ── Защита через атрибуты ─────────────────────────────────────────────

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, ResourceLocation id, ItemStack stack) {

        Multimap<Holder<Attribute>, AttributeModifier> map = LinkedHashMultimap.create();

        map.put(
                Attributes.ARMOR,
                new AttributeModifier(
                        id,
                        this.armor,
                        AttributeModifier.Operation.ADD_VALUE
                )
        );

        return map;
    }

     @Override
     public void curioTick(SlotContext slotContext, ItemStack stack) {
        //события в тик когда надето
     }

    // ── GeckoLib ──────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0,
                state -> PlayState.CONTINUE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private HeadbandRenderer renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(
                    @Nullable T livingEntity,
                    ItemStack itemStack,
                    @Nullable EquipmentSlot equipmentSlot,
                    @Nullable HumanoidModel<T> original
            ) {
                if (this.renderer == null)
                    this.renderer = new HeadbandRenderer();
                return this.renderer;
            }
        });
    }

    // ── Curios ────────────────────────────────────────────────────────────

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("face");
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}