package net.iwtengu.shinobiway.combat.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.iwtengu.shinobiway.combat.CombatAttachments;
import net.iwtengu.shinobiway.combat.CombatAttackRegistry;
import net.iwtengu.shinobiway.combat.CombatData;
import net.iwtengu.shinobiway.combat.CombatHelper;
import net.iwtengu.shinobiway.combat.client.animation.CombatWeaponRegistry.WeaponGroup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * ============================================================
 * TestClashCommand — /testclash [тип]
 * ============================================================
 * <p>
 * ВРЕМЕННАЯ команда для тестирования столкновений в одиночной игре.
 * УДАЛИ этот файл и его регистрацию из CommandRegister перед релизом.
 * <p>
 * ── Использование ─────────────────────────────────────────────
 * <p>
 * /testclash armed          — открыть окно как "вооружённый" (KATANA)
 * /testclash unarmed        — открыть окно как "безоружный" (EMPTY)
 * /testclash kunai          — открыть окно как кунай
 * /testclash perfect_armed  — открыть ИДЕАЛЬНОЕ окно (perfectClashWindow)
 * вооружённого — для теста перехвата оружия
 * /testclash status         — показать текущее состояние твоего окна
 * /testclash clear          — закрыть окно
 * <p>
 * ── Как тестировать ───────────────────────────────────────────
 * <p>
 * Сценарий 1: ARMED vs ARMED (звон, откидывание, без урона)
 * 1. /testclash armed          ← открываем окно как "KATANA"
 * 2. Сразу атакуй ЛКМ          ← твой удар попадёт в окно → клэш
 * <p>
 * Сценарий 2: UNARMED vs UNARMED (оба получают урон)
 * 1. Убери оружие из рук
 * 2. /testclash unarmed
 * 3. Сразу атакуй ЛКМ
 * <p>
 * Сценарий 3: UNARMED vs ARMED (безоружный получает урон)
 * 1. Убери оружие из рук
 * 2. /testclash armed
 * 3. Атакуй ЛКМ — ты получишь урон
 * <p>
 * Сценарий 4: Идеальный перехват оружия
 * 1. Убери оружие из рук
 * 2. /testclash perfect_armed   ← окно уже в perfectClashWindow
 * 3. Немедленно атакуй ЛКМ     ← должен произойти перехват
 * (у тебя появится оружие в руке если рука пуста)
 * <p>
 * ── КАК ЭТО РАБОТАЕТ ──────────────────────────────────────────
 * <p>
 * Команда открывает clashWindow прямо на ТЕБЕ же (на том же игроке).
 * AttackRequestPayload.handle() при ударе смотрит на сущности
 * в хитбоксе. Чтобы попасть в себя — команда телепортирует
 * "призрак" (на самом деле просто открывает окно на самом игроке,
 * а ClashDetector проверяет всех ServerPlayer в хитбоксе включая
 * себя если мы временно разрешим это).
 * <p>
 * НО: стандартный AttackRequestPayload пропускает `e == player`.
 * Поэтому команда использует другой подход — она создаёт фейковый
 * второй "слот" данных через временный статический буфер в
 * ClashTestBuffer, который AttackRequestPayload проверяет в debug-режиме.
 */
public class TestClashCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("testclash")
                        .requires(src -> src.hasPermission(0)) // доступно без OP в сингле
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("armed");
                                    builder.suggest("unarmed");
                                    builder.suggest("kunai");
                                    builder.suggest("perfect_armed");
                                    builder.suggest("status");
                                    builder.suggest("clear");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String type = StringArgumentType.getString(ctx, "type");
                                    return execute(ctx.getSource(), type);
                                })
                        )
                        // без аргумента — показать подсказку
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§eИспользование: /testclash <armed|unarmed|kunai|perfect_armed|status|clear>"
                            ), false);
                            return 1;
                        })
        );
    }

    private static int execute(CommandSourceStack source, String type) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Только для игрока"));
            return 0;
        }

        switch (type.toLowerCase()) {

            case "armed" -> {
                // Открываем окно как будто мы только что ударили с KATANA
                ClashTestBuffer.set(player, WeaponGroup.KATANA, false);
                source.sendSuccess(() -> Component.literal(
                        "§a[TestClash] Открыто окно: §6KATANA (armed)\n" +
                                "§7Теперь атакуй ЛКМ чтобы проверить клэш.\n" +
                                "§7Окно живёт §e" + CombatAttackRegistry.getClashWindow(WeaponGroup.KATANA) + " тиков§7."
                ), false);
            }

            case "unarmed" -> {
                ClashTestBuffer.set(player, WeaponGroup.EMPTY, false);
                source.sendSuccess(() -> Component.literal(
                        "§a[TestClash] Открыто окно: §fEMPTY (unarmed)\n" +
                                "§7Убери оружие и атакуй ЛКМ."
                ), false);
            }

            case "kunai" -> {
                ClashTestBuffer.set(player, WeaponGroup.KUNAI, false);
                source.sendSuccess(() -> Component.literal(
                        "§a[TestClash] Открыто окно: §bKUNAI\n" +
                                "§7Атакуй ЛКМ."
                ), false);
            }

            case "perfect_armed" -> {
                // Открываем уже как "идеальное" окно — perfect_clash
                ClashTestBuffer.set(player, WeaponGroup.KATANA, true);
                source.sendSuccess(() -> Component.literal(
                        "§a[TestClash] §dИДЕАЛЬНОЕ §aокно: KATANA\n" +
                                "§7Убери оружие и немедленно атакуй ЛКМ.\n" +
                                "§7→ Должен произойти §dперехват оружия§7."
                ), false);
            }

            case "status" -> {
                ClashTestBuffer.Entry entry = ClashTestBuffer.get(player);
                if (entry == null) {
                    source.sendSuccess(() -> Component.literal(
                            "§7[TestClash] Буфер пуст. Используй /testclash armed и т.д."
                    ), false);
                } else {
                    source.sendSuccess(() -> Component.literal(
                            "§e[TestClash] Буфер активен:\n" +
                                    "§7Группа: §f" + entry.group().name() + "\n" +
                                    "§7Идеальное окно: §f" + entry.perfect() + "\n" +
                                    "§7Осталось тиков: §f" + entry.ticksLeft()
                    ), false);
                }
            }

            case "clear" -> {
                ClashTestBuffer.clear(player);
                source.sendSuccess(() -> Component.literal(
                        "§7[TestClash] Буфер очищен."
                ), false);
            }

            default -> source.sendFailure(Component.literal(
                    "Неизвестный тип. Доступно: armed, unarmed, kunai, perfect_armed, status, clear"
            ));
        }

        return 1;
    }
}