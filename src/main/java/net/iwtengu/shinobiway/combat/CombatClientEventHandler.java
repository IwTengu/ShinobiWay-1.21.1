package net.iwtengu.shinobiway.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                CombatClientEventHandler                     ║
 * ║  Обрабатывает клиентский тик:                               ║
 * ║   - нажатие B (переключение боевого режима)                 ║
 * ║   - прыжок + направление (дэш)                              ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Все решения принимаются на сервере —
 * клиент только отправляет запросы.
 */
@EventBusSubscriber(modid = "shinobiway", value = Dist.CLIENT)
public class CombatClientEventHandler {

    /**
     * Флаг анти-спама дэша.
     * true = прыжок уже был обработан.
     */
    private static boolean jumpWasHeld = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        // Если игрока нет или открыт GUI — выходим
        if (player == null || mc.screen != null) {
            return;
        }

        handleToggleCombat();
        handleDash(mc, player);
    }

    // ─────────────────────────────────────────────────────────────
    // Переключение боевого режима
    // ─────────────────────────────────────────────────────────────

    private static void handleToggleCombat() {

        if (CombatKeyBindings.TOGGLE_COMBAT.consumeClick()) {
            CombatNetwork.sendToServer(new ToggleCombatPayload());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Дэш
    // ─────────────────────────────────────────────────────────────

    private static void handleDash(Minecraft mc, LocalPlayer player) {

        // Не отправляем пакеты если combat mode выключен
        if (!CombatHelper.isActive(player)) {
            jumpWasHeld = false;
            return;
        }

        boolean jumpNow = mc.options.keyJump.isDown();

        // Новое нажатие пробела
        if (jumpNow && !jumpWasHeld) {

            jumpWasHeld = true;

            boolean fwd  = mc.options.keyUp.isDown();
            boolean back = mc.options.keyDown.isDown();
            boolean left = mc.options.keyLeft.isDown();
            boolean rght = mc.options.keyRight.isDown();

            // Нужно хотя бы одно направление
            if (!fwd && !back && !left && !rght) {
                return;
            }

            // Yaw игрока
            float yaw = player.getYRot();

            // ─────────────────────────────────────────────────────
            // Forward vector
            // ─────────────────────────────────────────────────────

            double fwdX = -Math.sin(Math.toRadians(yaw));
            double fwdZ =  Math.cos(Math.toRadians(yaw));

            // ─────────────────────────────────────────────────────
            // Right vector
            // Исправлено:
            // раньше A/D были инвертированы
            // ─────────────────────────────────────────────────────

            double rightX = -Math.cos(Math.toRadians(yaw));
            double rightZ = -Math.sin(Math.toRadians(yaw));

            // ─────────────────────────────────────────────────────
            // Итоговое направление
            // ─────────────────────────────────────────────────────

            double dirX = 0;
            double dirZ = 0;

            if (fwd) {
                dirX += fwdX;
                dirZ += fwdZ;
            }

            if (back) {
                dirX -= fwdX;
                dirZ -= fwdZ;
            }

            if (rght) {
                dirX += rightX;
                dirZ += rightZ;
            }

            if (left) {
                dirX -= rightX;
                dirZ -= rightZ;
            }

            // Нормализация
            Vec3 dir = new Vec3(dirX, 0, dirZ).normalize();

            // Отправка пакета
            CombatNetwork.sendToServer(
                    new DashRequestPayload(dir.x, dir.z)
            );

            // ─────────────────────────────────────────────────────
            // Тут можно добавить:
            //  - particles
            //  - camera shake
            //  - client animation
            // ─────────────────────────────────────────────────────

        } else if (!jumpNow) {

            jumpWasHeld = false;
        }
    }
}