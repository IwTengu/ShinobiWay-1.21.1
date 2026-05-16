package net.iwtengu.shinobiway.combat;

import net.iwtengu.shinobiway.animation.AnimationController;
import net.iwtengu.shinobiway.combat.SyncCombatStatePayload;
import net.iwtengu.shinobiway.combat.CombatNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                       CombatHelper                          ║
 * ║  Публичный API боевой системы.                              ║
 * ║  Используй ТОЛЬКО этот класс из других частей мода.         ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * ── Шпаргалка: примеры использования из других классов ────────
 *
 *   // Разблокировать боевой режим (после достижения, квеста и т.д.):
 *   CombatHelper.unlock((ServerPlayer) player);
 *
 *   // Проверить активен ли режим:
 *   if (CombatHelper.isActive(player)) { ... }
 *
 *   // Запретить предмет, если режим НЕ активен:
 *   if (!CombatHelper.isActive(player)) return InteractionResultHolder.fail(stack);
 *
 *   // Запретить предмет, если режим АКТИВЕН:
 *   if (CombatHelper.isActive(player)) return InteractionResultHolder.fail(stack);
 *
 *   // Запретить вход в режим при определённом условии (в commonSetup):
 *   CombatHelper.addBlockCondition(p -> p.isInWater());
 *   CombatHelper.addBlockCondition(p -> p.hasEffect(MyEffects.STUN));
 *
 * ──────────────────────────────────────────────────────────────
 */
public class CombatHelper {

    /**
     * Список дополнительных условий блокировки входа в боевой режим.
     * Predicate<Player> → true означает "ЗАПРЕЩЕНО входить в режим".
     *
     * Встроенные проверки уже внутри canEnterCombat():
     *  - unlocked
     *  - не пассажир (лошадь, вагонетка, лодка и т.д.)
     *
     * Добавляй свои через addBlockCondition() при инициализации мода.
     */
    private static final List<Predicate<Player>> BLOCK_CONDITIONS = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    //  Получение данных
    // ─────────────────────────────────────────────────────────────
    private static final int TOGGLE_COOLDOWN_TICKS = 60; // 3 сек
    private static final java.util.Map<java.util.UUID, Integer> TOGGLE_CD = new java.util.HashMap<>();

    public static boolean canToggle(ServerPlayer player) {
        return TOGGLE_CD.getOrDefault(player.getUUID(), 0) <= 0;
    }

    public static void setToggleCooldown(ServerPlayer player) {
        TOGGLE_CD.put(player.getUUID(), TOGGLE_COOLDOWN_TICKS);
    }

    public static void tickCooldowns(ServerPlayer player) {
        int cd = TOGGLE_CD.getOrDefault(player.getUUID(), 0);
        if (cd > 0) TOGGLE_CD.put(player.getUUID(), cd - 1);
    }
    /**
     * Получить CombatData игрока.
     * getData() создаёт дефолтный объект если его ещё нет — никогда не null.
     */
    public static CombatData getData(Player player) {
        return player.getData(CombatAttachments.COMBAT_DATA.get());
    }

    // ─────────────────────────────────────────────────────────────
    //  Разблокировка
    // ─────────────────────────────────────────────────────────────

    /**
     * Открыть доступ к боевому режиму игроку.
     * Вызывай из любого места — после достижения, квеста, события и т.д.
     * Только на сервере (ServerPlayer)!
     */
    public static void unlock(ServerPlayer player) {
        CombatData data = getData(player);
        data.setUnlocked(true);
        // setData нужен для пометки "данные изменились" (NeoForge сериализует)
        player.setData(CombatAttachments.COMBAT_DATA.get(), data);
        syncToClient(player);
    }

    /**
     * Закрыть доступ к боевому режиму.
     * Если режим был активен — деактивирует его.
     */
    public static void lock(ServerPlayer player) {
        CombatData data = getData(player);
        data.setUnlocked(false);
        data.setActive(false);
        player.setData(CombatAttachments.COMBAT_DATA.get(), data);
        syncToClient(player);
    }

    /**
     * Проверить, разблокирован ли режим.
     * Работает на клиенте и сервере.
     */
    public static boolean isUnlocked(Player player) {
        return getData(player).isUnlocked();
    }

    // ─────────────────────────────────────────────────────────────
    //  Переключение режима
    // ─────────────────────────────────────────────────────────────

    /**
     * Попытаться активировать боевой режим.
     * Проверяет все встроенные и пользовательские условия.
     * @return true — если режим успешно активирован
     */
    public static boolean tryActivate(ServerPlayer player) {
        if (!canEnterCombat(player)) return false;

        CombatData data = getData(player);
        data.setActive(true);
        player.setData(CombatAttachments.COMBAT_DATA.get(), data);
        syncToClient(player);
        return true;
    }

    /**
     * Деактивировать боевой режим.
     */
    public static void deactivate(ServerPlayer player) {
        CombatData data = getData(player);
        data.setActive(false);
        player.setData(CombatAttachments.COMBAT_DATA.get(), data);
        syncToClient(player);
        AnimationController.stopAll(player);
    }

    /**
     * Активен ли боевой режим прямо сейчас.
     * Работает на клиенте и сервере.
     */
    public static boolean isActive(Player player) {
        return getData(player).isActive();
    }

    // ─────────────────────────────────────────────────────────────
    //  Условия блокировки
    // ─────────────────────────────────────────────────────────────

    /**
     * Добавить условие, при котором нельзя войти в боевой режим.
     * Добавляй в ShinobiWayMod.commonSetup().
     *
     * Примеры:
     *   CombatHelper.addBlockCondition(p -> p.isInWater());
     *   CombatHelper.addBlockCondition(p -> p.getFoodData().getFoodLevel() < 3);
     */
    public static void addBlockCondition(Predicate<Player> condition) {
        BLOCK_CONDITIONS.add(condition);
    }

    /**
     * Проверяет ВСЕ условия входа в боевой режим.
     * Встроенные:
     *  1. Режим разблокирован
     *  2. Игрок не является пассажиром (лошадь / вагонетка / лодка / и т.д.)
     */
    public static boolean canEnterCombat(Player player) {
        if (!isUnlocked(player)) return false;
        // isPassenger() — true для любого транспортного средства
        if (player.isPassenger()) return false;

        for (Predicate<Player> condition : BLOCK_CONDITIONS) {
            if (condition.test(player)) return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    //  Синхронизация
    // ─────────────────────────────────────────────────────────────

    /**
     * Отправить текущее состояние боевого режима клиенту игрока.
     * Вызывай после любого изменения данных на сервере.
     */
    public static void syncToClient(ServerPlayer player) {
        CombatData data = getData(player);
        CombatNetwork.sendToPlayer(
                new SyncCombatStatePayload(data.isUnlocked(), data.isActive()),
                player
        );
    }
}