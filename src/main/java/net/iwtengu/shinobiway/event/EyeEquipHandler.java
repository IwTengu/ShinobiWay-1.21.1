package net.iwtengu.shinobiway.event;

import net.iwtengu.shinobiway.item.ModItems;
import net.iwtengu.shinobiway.util.EyeOwnerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;

@EventBusSubscriber(modid = "shinobiway")
public class EyeEquipHandler {

    @SubscribeEvent
    public static void onCurioEquip(CurioCanEquipEvent event) {

        // Проверяем что надевается именно наш глаз
        if (!event.getStack().is(ModItems.EYE.get())) {
            return;
        }

        // Работаем только на сервере
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Привязываем глаз к игроку если ещё не привязан
        EyeOwnerHelper.bindIfNeeded(event.getStack(), player);
    }
}
