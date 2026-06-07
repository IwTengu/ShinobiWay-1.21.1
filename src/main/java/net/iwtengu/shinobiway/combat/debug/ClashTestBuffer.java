package net.iwtengu.shinobiway.combat.debug;

import net.iwtengu.shinobiway.combat.CombatAttackRegistry;
import net.iwtengu.shinobiway.combat.client.animation.CombatWeaponRegistry.WeaponGroup;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ============================================================
 *  ClashTestBuffer — временный буфер для тестирования клэша.
 * ============================================================
 *
 * ВРЕМЕННЫЙ КЛАСС. Удали перед релизом вместе с TestClashCommand.
 *
 * Симулирует "второго игрока" — хранит фейковое окно столкновения
 * для самого игрока. AttackRequestPayload в debug-режиме проверяет
 * этот буфер прежде чем искать реальных игроков в хитбоксе.
 *
 * Тики убывают каждый серверный тик через CombatServerEventHandler.
 */
public class ClashTestBuffer {

    public record Entry(WeaponGroup group, boolean perfect, int ticksLeft) {
        Entry tick() {
            return new Entry(group, perfect, ticksLeft - 1);
        }
        boolean expired() {
            return ticksLeft <= 0;
        }
    }

    private static final Map<UUID, Entry> BUFFER = new HashMap<>();

    /**
     * Открыть тестовое окно для игрока.
     *
     * @param perfect true — окно сразу в зоне perfectClashWindow
     *                (для теста перехвата оружия)
     */
    public static void set(ServerPlayer player, WeaponGroup group, boolean perfect) {
        int window = perfect
                ? CombatAttackRegistry.getPerfectClashWindow(group)  // уже в идеальной зоне
                : CombatAttackRegistry.getClashWindow(group);        // полное окно
        BUFFER.put(player.getUUID(), new Entry(group, perfect, window));
    }

    /** Получить запись (null если нет или истекла) */
    public static Entry get(ServerPlayer player) {
        Entry entry = BUFFER.get(player.getUUID());
        if (entry == null || entry.expired()) {
            BUFFER.remove(player.getUUID());
            return null;
        }
        return entry;
    }

    /** Очистить буфер игрока */
    public static void clear(ServerPlayer player) {
        BUFFER.remove(player.getUUID());
    }

    /** Тикнуть все записи — вызывать каждый серверный тик */
    public static void tickAll() {
        BUFFER.replaceAll((uuid, entry) -> entry.tick());
        BUFFER.entrySet().removeIf(e -> e.getValue().expired());
    }

    /** Есть ли активное тестовое окно у игрока */
    public static boolean has(ServerPlayer player) {
        return get(player) != null;
    }
}