package net.iwtengu.shinobiway.combat.clash;

import net.iwtengu.shinobiway.combat.CombatAttachments;
import net.iwtengu.shinobiway.combat.CombatAttackRegistry;
import net.iwtengu.shinobiway.combat.CombatData;
import net.iwtengu.shinobiway.combat.CombatHelper;
import net.iwtengu.shinobiway.combat.CombatNetwork;
import net.iwtengu.shinobiway.combat.client.animation.CombatWeaponRegistry;
import net.iwtengu.shinobiway.combat.client.animation.CombatWeaponRegistry.WeaponGroup;
import net.iwtengu.shinobiway.combat.debug.ClashTestBuffer;
import net.iwtengu.shinobiway.combat.network.packets.ClashResultPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                      ClashDetector                          ║
 * ║  Серверная логика столкновения ударов.                      ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class ClashDetector {

    // ── Настройка ─────────────────────────────────────────────────
    private static final float  UNARMED_CLASH_DAMAGE    = 2.0f;
    private static final float  UNARMED_VS_ARMED_DAMAGE = 4.0f;
    private static final double KNOCKBACK_STRENGTH      = 0.6;

    // ─────────────────────────────────────────────────────────────
    //  Обычный клэш (два реальных игрока)
    // ─────────────────────────────────────────────────────────────

    /**
     * Проверить столкновение между атакующим и целью.
     * @return true — клэш произошёл, стандартный урон не нужен
     */
    public static boolean tryClash(ServerPlayer attacker, ServerPlayer defender,
                                   WeaponGroup attackerGroup) {
        if (!CombatHelper.isActive(defender)) return false;

        CombatData defData = CombatHelper.getData(defender);
        if (!defData.hasClashWindow()) return false;

        WeaponGroup defenderGroup = groupByIndex(defData.getClashWeaponIndex());

        // Оружие vs безоружный — столкновения нет, продолжаем стандартный урон
        if (attackerGroup != WeaponGroup.EMPTY && defenderGroup == WeaponGroup.EMPTY) {
            return false;
        }

        ClashType clashType = resolveClashType(
                attackerGroup, defenderGroup, defData
        );
        if (clashType == null) return false;

        applyClash(attacker, defender, attackerGroup, defenderGroup, clashType);
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    //  [DEBUG] Клэш с тестовым буфером (один игрок, сингл)
    // ─────────────────────────────────────────────────────────────

    /**
     * Симулирует клэш с самим собой используя данные из ClashTestBuffer.
     * Вместо второго игрока — фейковый "defender" из буфера.
     * Урон и отталкивание применяются только к самому игроку.
     *
     * ВРЕМЕННЫЙ МЕТОД. Удали вместе с ClashTestBuffer.
     */
    public static void tryClashWithBuffer(ServerPlayer player, WeaponGroup attackerGroup,
                                          ClashTestBuffer.Entry testEntry) {
        WeaponGroup defenderGroup = testEntry.group();
        boolean isPerfect = testEntry.perfect();

        // Определяем тип клэша
        ClashType clashType;
        boolean attackerArmed  = attackerGroup  != WeaponGroup.EMPTY;
        boolean defenderArmed  = defenderGroup  != WeaponGroup.EMPTY;

        if (attackerArmed && defenderArmed) {
            clashType = ClashType.ARMED_VS_ARMED;
        } else if (!attackerArmed && !defenderArmed) {
            clashType = ClashType.UNARMED_VS_UNARMED;
        } else if (!attackerArmed && defenderArmed) {
            clashType = isPerfect
                    ? ClashType.UNARMED_VS_ARMED_PERFECT
                    : ClashType.UNARMED_VS_ARMED;
        } else {
            // ARMED vs UNARMED — нет клэша
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§c[TestClash] Нет клэша: ARMED атакует UNARMED — стандартный урон.")
            );
            return;
        }

        // Применяем — одиночный режим: урон/эффекты только на самого игрока
        applyClashSolo(player, attackerGroup, defenderGroup, clashType);
    }

    /**
     * Применить клэш на одного игрока (тестовый режим).
     * Нет второго игрока — отталкивание назад, урон себе.
     */
    private static void applyClashSolo(ServerPlayer player, WeaponGroup atkGroup,
                                       WeaponGroup defGroup, ClashType type) {
        // Небольшое отталкивание назад (вместо взаимного)
        Vec3 back = player.getLookAngle().scale(-KNOCKBACK_STRENGTH);
        player.setDeltaMovement(player.getDeltaMovement().add(back));
        player.hurtMarked = true;

        switch (type) {
            case ARMED_VS_ARMED -> {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§6[TestClash] §fARMED vs ARMED — §aзвон клинков, урон не нанесён."));
                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(ClashResultPayload.Effect.WEAPONS_CLASH), player);
            }
            case UNARMED_VS_UNARMED -> {
                player.hurt(player.damageSources().fall(), UNARMED_CLASH_DAMAGE);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§6[TestClash] §fUNARMED vs UNARMED — §curон " + UNARMED_CLASH_DAMAGE + " себе."));
                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(ClashResultPayload.Effect.FIST_CLASH), player);
            }
            case UNARMED_VS_ARMED -> {
                player.hurt(player.damageSources().fall(), UNARMED_VS_ARMED_DAMAGE);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§6[TestClash] §fUNARMED vs ARMED — §curон " + UNARMED_VS_ARMED_DAMAGE + " безоружному."));
                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(ClashResultPayload.Effect.UNARMED_VS_ARMED), player);
            }
            case UNARMED_VS_ARMED_PERFECT -> {
                // В соло — просто выбрасываем своё оружие из второй руки как "перехваченное"
                // (у тебя нет оружия в руке для теста — поэтому просто сообщение)
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§d[TestClash] §fИДЕАЛЬНЫЙ ПЕРЕХВАТ! В реальной игре ты бы забрал оружие."));
                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(ClashResultPayload.Effect.PERFECT_DISARM), player);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Определение типа клэша
    // ─────────────────────────────────────────────────────────────

    private static ClashType resolveClashType(WeaponGroup atkGroup, WeaponGroup defGroup,
                                              CombatData defData) {
        boolean attackerArmed = atkGroup != WeaponGroup.EMPTY;
        boolean defenderArmed = defGroup != WeaponGroup.EMPTY;

        if (attackerArmed && defenderArmed)   return ClashType.ARMED_VS_ARMED;
        if (!attackerArmed && !defenderArmed) return ClashType.UNARMED_VS_UNARMED;

        if (!attackerArmed && defenderArmed) {
            int perfectWindow = CombatAttackRegistry.getPerfectClashWindow(defGroup);
            return defData.getClashWindowRemaining() <= perfectWindow
                    ? ClashType.UNARMED_VS_ARMED_PERFECT
                    : ClashType.UNARMED_VS_ARMED;
        }

        return null; // ARMED vs UNARMED — не клэш
    }

    // ─────────────────────────────────────────────────────────────
    //  Применение клэша (два реальных игрока)
    // ─────────────────────────────────────────────────────────────

    private static void applyClash(ServerPlayer attacker, ServerPlayer defender,
                                   WeaponGroup atkGroup, WeaponGroup defGroup,
                                   ClashType type) {

        // ===== DEBUG CHAT =====
        attacker.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        "§6[CLASH] §fСтолкновение! Тип: §e" + type.name()
                                + " §7(" + attacker.getName().getString()
                                + " vs "
                                + defender.getName().getString() + ")"));

        defender.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        "§6[CLASH] §fСтолкновение! Тип: §e" + type.name()
                                + " §7(" + attacker.getName().getString()
                                + " vs "
                                + defender.getName().getString() + ")"));
        // ======================

        // Закрываем окна обоих
        CombatData atkData = CombatHelper.getData(attacker);
        CombatData defData = CombatHelper.getData(defender);

        atkData.closeClashWindow();
        defData.closeClashWindow();

        attacker.setData(CombatAttachments.COMBAT_DATA.get(), atkData);
        defender.setData(CombatAttachments.COMBAT_DATA.get(), defData);

        switch (type) {

            case ARMED_VS_ARMED -> {
                applyMutualKnockback(attacker, defender);

                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(
                                ClashResultPayload.Effect.WEAPONS_CLASH), attacker);

                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(
                                ClashResultPayload.Effect.WEAPONS_CLASH), defender);
            }

            case UNARMED_VS_UNARMED -> {
                attacker.hurt(
                        attacker.damageSources().playerAttack(defender),
                        UNARMED_CLASH_DAMAGE);

                defender.hurt(
                        defender.damageSources().playerAttack(attacker),
                        UNARMED_CLASH_DAMAGE);

                applyMutualKnockback(attacker, defender);

                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(
                                ClashResultPayload.Effect.FIST_CLASH), attacker);

                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(
                                ClashResultPayload.Effect.FIST_CLASH), defender);
            }

            case UNARMED_VS_ARMED -> {
                attacker.hurt(
                        attacker.damageSources().playerAttack(defender),
                        UNARMED_VS_ARMED_DAMAGE);

                applyMutualKnockback(attacker, defender);

                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(
                                ClashResultPayload.Effect.UNARMED_VS_ARMED), attacker);

                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(
                                ClashResultPayload.Effect.UNARMED_VS_ARMED), defender);
            }

            case UNARMED_VS_ARMED_PERFECT -> {

                ItemStack stolenWeapon = defender.getMainHandItem().copy();

                if (!stolenWeapon.isEmpty()) {

                    defender.setItemInHand(
                            net.minecraft.world.InteractionHand.MAIN_HAND,
                            ItemStack.EMPTY);

                    if (attacker.getMainHandItem().isEmpty()) {

                        attacker.setItemInHand(
                                net.minecraft.world.InteractionHand.MAIN_HAND,
                                stolenWeapon);

                    } else {

                        ItemEntity drop = new ItemEntity(
                                attacker.level(),
                                attacker.getX(),
                                attacker.getY() + 0.5,
                                attacker.getZ(),
                                stolenWeapon);

                        drop.setPickUpDelay(0);
                        attacker.level().addFreshEntity(drop);
                    }
                }

                applyMutualKnockback(attacker, defender);

                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(
                                ClashResultPayload.Effect.PERFECT_DISARM), attacker);

                CombatNetwork.sendToPlayer(
                        new ClashResultPayload(
                                ClashResultPayload.Effect.PERFECT_DISARM), defender);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Утилиты
    // ─────────────────────────────────────────────────────────────

    private static void applyMutualKnockback(ServerPlayer a, ServerPlayer b) {
        Vec3 aToB = b.position().subtract(a.position()).normalize().scale(KNOCKBACK_STRENGTH);
        a.setDeltaMovement(a.getDeltaMovement().add(aToB.scale(-1)));
        b.setDeltaMovement(b.getDeltaMovement().add(aToB));
        a.hurtMarked = true;
        b.hurtMarked = true;
    }

    private static WeaponGroup groupByIndex(int index) {
        for (WeaponGroup g : WeaponGroup.values()) {
            if (g.index == index) return g;
        }
        return WeaponGroup.EMPTY;
    }
}