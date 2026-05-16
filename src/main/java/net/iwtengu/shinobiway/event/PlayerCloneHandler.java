package net.iwtengu.shinobiway.event;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerCloneHandler {

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerCloneHandler::onPlayerClone);
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {

        if (!event.isWasDeath()) return;

        var oldData = event.getOriginal().getPersistentData();

        if (oldData.contains("shinobiway_data")) {

            event.getEntity().getPersistentData().put(
                    "shinobiway_data",
                    oldData.getCompound("shinobiway_data").copy()
            );
        }
    }
}