package net.iwtengu.shinobiway.combat;

import net.iwtengu.shinobiway.animation.AnimationController;
import net.iwtengu.shinobiway.combat.SyncCombatStatePayload;
import net.iwtengu.shinobiway.combat.CombatNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                       CombatHelper                          ║
 * ║  Публичный API боевой системы.                              ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * ── Шпаргалка ─────────────────────────────────────────────────
 *
 *   CombatHelper.unlock((ServerPlayer) player);
 *   if (CombatHelper.isActive(player)) { ... }
 *   if (!CombatHelper.isActive(player)) return InteractionResultHolder.fail(stack);
 *   CombatHelper.addBlockCondition(p -> p.isInWater());
 */
public class CombatHelper {

    private static final List<Predicate<Player>> BLOCK_CONDITIONS = new ArrayList<>();

    // ── Кулдаун переключения режима ───────────────────────────────
    private static final int TOGGLE_COOLDOWN_TICKS = 60; // 3 сек
    private static final Map<UUID, Integer> TOGGLE_CD = new HashMap<>();

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

    // ─────────────────────────────────────────────────────────────
    //  Данные
    // ─────────────────────────────────────────────────────────────

    public static CombatData getData(Player player) {
        return player.getData(CombatAttachments.COMBAT_DATA.get());
    }

    // ─────────────────────────────────────────────────────────────
    //  Разблокировка
    // ─────────────────────────────────────────────────────────────

    public static void unlock(ServerPlayer player) {
        CombatData data = getData(player);
        data.setUnlocked(true);
        player.setData(CombatAttachments.COMBAT_DATA.get(), data);
        syncToClient(player);
    }

    public static void lock(ServerPlayer player) {
        CombatData data = getData(player);
        data.setUnlocked(false);
        data.setActive(false);
        player.setData(CombatAttachments.COMBAT_DATA.get(), data);
        syncToClient(player);
    }

    public static boolean isUnlocked(Player player) {
        return getData(player).isUnlocked();
    }

    // ─────────────────────────────────────────────────────────────
    //  Переключение режима
    // ─────────────────────────────────────────────────────────────

    public static boolean tryActivate(ServerPlayer player) {
        if (!canEnterCombat(player)) return false;
        CombatData data = getData(player);
        data.setActive(true);
        player.setData(CombatAttachments.COMBAT_DATA.get(), data);
        syncToClient(player);
        return true;
    }

    public static void deactivate(ServerPlayer player) {
        CombatData data = getData(player);
        data.setActive(false);
        // Сбрасываем боевые состояния при выходе из режима
        data.resetCombo();
        data.closeClashWindow();
        player.setData(CombatAttachments.COMBAT_DATA.get(), data);
        syncToClient(player);
        AnimationController.stopAll(player);
    }

    public static boolean isActive(Player player) {
        return getData(player).isActive();
    }

    // ─────────────────────────────────────────────────────────────
    //  Условия блокировки
    // ─────────────────────────────────────────────────────────────

    public static void addBlockCondition(Predicate<Player> condition) {
        BLOCK_CONDITIONS.add(condition);
    }

    public static boolean canEnterCombat(Player player) {
        if (!isUnlocked(player)) return false;
        if (player.isPassenger()) return false;
        for (Predicate<Player> condition : BLOCK_CONDITIONS) {
            if (condition.test(player)) return false;
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    //  Синхронизация
    // ─────────────────────────────────────────────────────────────

    public static void syncToClient(ServerPlayer player) {
        CombatData data = getData(player);
        CombatNetwork.sendToPlayer(
                new SyncCombatStatePayload(data.isUnlocked(), data.isActive()),
                player
        );
    }
}