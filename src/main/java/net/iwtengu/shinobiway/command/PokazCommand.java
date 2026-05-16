package net.iwtengu.shinobiway.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.iwtengu.shinobiway.animation.ModNetwork;
import net.iwtengu.shinobiway.animation.PlayAnimationPacket;
import net.iwtengu.shinobiway.player.ShurikenMasteryData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PokazCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("pokaz")

                        // =====================
                        // /pokaz
                        // =====================
                        .executes(context -> {

                            CommandSourceStack source = context.getSource();

                            if (!(source.getEntity() instanceof ServerPlayer player)) {
                                source.sendFailure(Component.literal("Только игрок"));
                                return 0;
                            }

                            int mastery = ShurikenMasteryData.getShurikenMastery(player);

                            player.sendSystemMessage(
                                    Component.literal("§aShurikenMastery: §e" + mastery)
                            );

                            return 1;
                        })

                        // =====================
                        // /pokaz set <value>
                        // =====================
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("value", IntegerArgumentType.integer())
                                                        .executes(context -> {

                                                            ServerPlayer player = context.getSource().getPlayerOrException();

                                                            int value = IntegerArgumentType.getInteger(context, "value");

                                                            if (value < 0) value = 0;
                                                            if (value > 5000) value = 5000;

                                                            ShurikenMasteryData.setShurikenMastery(player, value);

                                                            player.sendSystemMessage(
                                                                    Component.literal("§aУстановлено: §e" + value)
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )
        );
    }
}