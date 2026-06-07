package net.iwtengu.shinobiway.event;

import net.iwtengu.shinobiway.network.EyeUnlockPayload;
import net.iwtengu.shinobiway.player.PlayerEyeData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(modid = "shinobiway")
public class PlayerLoginHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean unlocked = player.getData(PlayerEyeData.EYE_SLOTS_UNLOCKED);

        if (unlocked) {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.setSlotActive("left_eye",  0, true);
                handler.setSlotActive("right_eye", 0, true);
            });
        } else {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.setSlotActive("left_eye",  0, false);
                handler.setSlotActive("right_eye", 0, false);
            });
        }

        // Синхронизируем флаг с клиентом при входе в мир
        PacketDistributor.sendToPlayer(player, new EyeUnlockPayload(unlocked));
    }
}