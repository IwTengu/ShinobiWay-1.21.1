package net.iwtengu.shinobiway.chakra;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class ChakraRegenTick {

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 1 раз в секунду
        if (player.tickCount % 20 != 0) return;

        ChakraData chakra = ChakraAttachment.get(player);

        if (!chakra.isUnlocked()) return;

        float regen =
                1f // 🔥 базовая регенерация
                        + ChakraRegenSources.getBonus(player); // 🔥 ВСЕ ДОП. ИСТОЧНИКИ

        chakra.add(regen);

        ChakraSyncPacket.send(player);
    }
}