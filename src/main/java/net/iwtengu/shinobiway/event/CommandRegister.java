package net.iwtengu.shinobiway.event;

import net.iwtengu.shinobiway.combat.debug.TestClashCommand;
import net.iwtengu.shinobiway.command.PokazCommand;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandRegister {

    public static void register() {

        NeoForge.EVENT_BUS.addListener(CommandRegister::onCommandsRegister);
    }

    private static void onCommandsRegister(RegisterCommandsEvent event) {

        PokazCommand.register(event.getDispatcher());
        TestClashCommand.register(event.getDispatcher());
    }
}