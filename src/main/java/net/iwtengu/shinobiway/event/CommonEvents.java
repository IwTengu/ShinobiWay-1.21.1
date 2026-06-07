package net.iwtengu.shinobiway.event;

import net.iwtengu.shinobiway.ShinobiWay;
import net.iwtengu.shinobiway.item.custom.HeadbandItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(modid = ShinobiWay.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();

        CuriosApi.getCuriosInventory(entity).ifPresent(handler -> {
            handler.getStacksHandler("face").ifPresent(stacksHandler -> {
                ItemStack stack = stacksHandler.getStacks().getStackInSlot(0);

                //if (stack.getItem() instanceof HeadbandItem) {
                  //  stack.hurtAndBreak(1, entity, EquipmentSlot.HEAD);
                // }
            });
        });
    }
}