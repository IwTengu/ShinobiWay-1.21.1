package net.iwtengu.shinobiway.combat.client.animation;

import net.iwtengu.shinobiway.combat.CombatHelper;
import net.iwtengu.shinobiway.combat.CombatNetwork;
import net.iwtengu.shinobiway.combat.network.packets.AttackRequestPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

/**
 * ============================================================
 *  CombatAnimationTicker — клиентский тик + ввод.
 * ============================================================
 *
 * Обрабатывает:
 *   - ClientTickEvent.Post  — тик локомоционных анимаций
 *   - InputEvent.InteractionKeyMappingTriggered — ЛКМ → атака
 */
@EventBusSubscriber(modid = "shinobiway", value = Dist.CLIENT)
public class CombatAnimationTicker {

    private static final CombatAnimationHandler HANDLER = new CombatAnimationHandler();
    private static boolean wasActive = false;
    private static boolean wasScreenClosed = true;

    // ─────────────────────────────────────────────────────────
    //  Тик
    // ─────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            wasActive = false;
            wasScreenClosed = true;
            return;
        }

        boolean isActive = CombatHelper.isActive(player);
        boolean screenOpen = mc.screen != null;

        // Экран открылся — останавливаем анимацию
        if (screenOpen && wasScreenClosed && isActive) {
            HANDLER.reset(player);
        }

        if (screenOpen) {
            wasScreenClosed = false;
            return;
        }

        // Экран закрылся — форсируем перезапуск анимации
        if (!wasScreenClosed) {
            HANDLER.forceRefresh();
        }
        wasScreenClosed = true;

        if (wasActive && !isActive) {
            HANDLER.reset(player);
        }

        if (isActive) {
            HANDLER.tick(player);
        }

        wasActive = isActive;
    }

    // ─────────────────────────────────────────────────────────
    //  ЛКМ — запрос атаки
    // ─────────────────────────────────────────────────────────

    /**
     * Перехватываем нажатие ЛКМ.
     * Если боевой режим активен — отправляем AttackRequestPayload
     * на сервер вместо стандартного удара Minecraft.
     *
     * event.setCanceled(true) — отменяем стандартный удар чтобы
     * не было двойного урона (наш + ванильный).
     */
    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        // Реагируем только на ЛКМ (attack), не на ПКМ (use)
        if (!event.isAttack()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // Только в боевом режиме
        if (!CombatHelper.isActive(player)) return;

        // Отменяем стандартный удар Minecraft
        event.setCanceled(true);

        // Отправляем запрос атаки на сервер
        CombatNetwork.sendToServer(new AttackRequestPayload());
    }

    // ─────────────────────────────────────────────────────────
    //  Статические методы для внешних вызовов
    // ─────────────────────────────────────────────────────────

    /** Вызывается из PlayDashAnimationPayload.handle() */
    public static void playDash(LocalPlayer player) {
        HANDLER.playDash(player);
    }

    /**
     * Вызывается из PlayAttackAnimationPayload.handle().
     * @param hitIndex   номер удара (0/1/2)
     * @param groupIndex индекс группы оружия
     */
    public static void playAttack(LocalPlayer player, int hitIndex, int groupIndex) {
        HANDLER.playAttack(player, hitIndex, groupIndex);
    }
}