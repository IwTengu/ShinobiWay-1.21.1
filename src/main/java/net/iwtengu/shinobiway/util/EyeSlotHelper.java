package net.iwtengu.shinobiway.util;

import net.iwtengu.shinobiway.item.ModItems;
import com.google.common.collect.LinkedHashMultimap;
import net.iwtengu.shinobiway.player.PlayerEyeData;
import net.iwtengu.shinobiway.network.EyeUnlockPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

public class EyeSlotHelper {

    private static final ResourceLocation LEFT_EYE_MODIFIER =
            ResourceLocation.fromNamespaceAndPath("shinobiway", "unlock_left_eye");
    private static final ResourceLocation RIGHT_EYE_MODIFIER =
            ResourceLocation.fromNamespaceAndPath("shinobiway", "unlock_right_eye");

    public static void unlockEyeSlots(ServerPlayer player) {

        if (player.getData(PlayerEyeData.EYE_SLOTS_UNLOCKED)) {
            return;
        }

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {

            // Открываем слоты через slot modifier
            var map = LinkedHashMultimap.<String, AttributeModifier>create();
            map.put("left_eye", new AttributeModifier(
                    LEFT_EYE_MODIFIER, 1, AttributeModifier.Operation.ADD_VALUE
            ));
            map.put("right_eye", new AttributeModifier(
                    RIGHT_EYE_MODIFIER, 1, AttributeModifier.Operation.ADD_VALUE
            ));
            handler.addPermanentSlotModifiers(map);
        });

        player.setData(PlayerEyeData.EYE_SLOTS_UNLOCKED, true);

        // Отправляем клиенту сигнал о разблокировке
        PacketDistributor.sendToPlayer(player, new EyeUnlockPayload(true));

        // Кладём глаза на следующий тик — слоты должны успеть проинициализироваться
        player.getServer().execute(() -> {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {

                handler.getStacksHandler("left_eye").ifPresent(s -> {
                    ItemStack eye = new ItemStack(ModItems.EYE.get());
                    EyeOwnerHelper.bindIfNeeded(eye, player); // привязываем до вставки
                    s.getStacks().setStackInSlot(0, eye);
                });

                handler.getStacksHandler("right_eye").ifPresent(s -> {
                    ItemStack eye = new ItemStack(ModItems.EYE.get());
                    EyeOwnerHelper.bindIfNeeded(eye, player); // привязываем до вставки
                    s.getStacks().setStackInSlot(0, eye);
                });
            });
        });
    }
}