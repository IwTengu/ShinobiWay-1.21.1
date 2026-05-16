package net.iwtengu.shinobiway.combat;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                  CombatServerEventHandler                   ║
 * ║  Серверные события боевой системы.                          ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Обрабатывает:
 *  - PlayerTickEvent.Post
 *  - LivingDeathEvent
 *  - PlayerLoggedInEvent
 *  - PlayerChangedDimensionEvent
 *
 * copyOnDeath для unlocked уже настроен
 * в CombatAttachments через .copyOnDeath().
 */
@EventBusSubscriber(modid = "shinobiway")
public class CombatServerEventHandler {

    // ─────────────────────────────────────────────────────────────
    // Тик игрока
    // ─────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {

        // Только сервер
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CombatData data = CombatHelper.getData(player);

        // Уменьшаем кулдаун
        data.tickDashCooldown();

        // ─────────────────────────────────────────────────────────
        // Тут можно добавить:
        //  - regen stamina
        //  - auto deactivate
        //  - combo timeout
        // ─────────────────────────────────────────────────────────
    }

    // ─────────────────────────────────────────────────────────────
    // Смерть игрока
    // ─────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CombatData data = CombatHelper.getData(player);

        // Выключаем combat mode
        if (data.isActive()) {

            data.setActive(false);

            player.setData(
                    CombatAttachments.COMBAT_DATA.get(),
                    data
            );

            // Обновляем клиент
            CombatHelper.syncToClient(player);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Вход игрока
    // ─────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CombatData data = CombatHelper.getData(player);

        // На всякий случай сбрасываем active
        if (data.isActive()) {

            data.setActive(false);

            player.setData(
                    CombatAttachments.COMBAT_DATA.get(),
                    data
            );
        }

        CombatHelper.syncToClient(player);
    }

    // ─────────────────────────────────────────────────────────────
    // Смена измерения
    // ─────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerChangedDimension(
            PlayerEvent.PlayerChangedDimensionEvent event
    ) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CombatHelper.syncToClient(player);
    }
}