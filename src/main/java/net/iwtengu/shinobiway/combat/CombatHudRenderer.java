package net.iwtengu.shinobiway.combat;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                    CombatHudRenderer                        ║
 * ║  Рисует HUD поверх стандартного интерфейса Minecraft.       ║
 * ║  Показывает: статус режима + полоску кулдауна дэша.         ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Позиция: левый нижний угол, над хотбаром.
 */
@EventBusSubscriber(modid = "shinobiway", value = Dist.CLIENT)
public class CombatHudRenderer {

    // Цвета ARGB
    private static final int COLOR_ACTIVE_TEXT = 0xFFFF4444;
    private static final int COLOR_IDLE_TEXT   = 0xFF888888;

    private static final int COLOR_DASH_FILL  = 0xFF44AAFF;
    private static final int COLOR_DASH_BG    = 0xFF333333;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        // HUD только если режим разблокирован
        if (!CombatHelper.isUnlocked(mc.player)) {
            return;
        }

        CombatData data = CombatHelper.getData(mc.player);

        renderHud(event, mc, data);
    }

    private static void renderHud(RenderGuiEvent.Post event,
                                  Minecraft mc,
                                  CombatData data) {

        var gui = event.getGuiGraphics();

        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Позиция HUD
        int x = 10;
        int y = screenHeight - 55;

        // ─────────────────────────────────────────────────────────
        // Статус режима
        // ─────────────────────────────────────────────────────────

        String label = data.isActive()
                ? "⚔ БОЕВОЙ РЕЖИМ"
                : "○ Боевой режим";

        int color = data.isActive()
                ? COLOR_ACTIVE_TEXT
                : COLOR_IDLE_TEXT;

        gui.drawString(
                mc.font,
                label,
                x,
                y,
                color,
                true
        );

        // ─────────────────────────────────────────────────────────
        // Полоска дэша
        // ─────────────────────────────────────────────────────────

        if (data.isActive()) {

            int barX = x;
            int barY = y + 12;

            int barWidth  = 80;
            int barHeight = 4;

            // Фон
            gui.fill(
                    barX,
                    barY,
                    barX + barWidth,
                    barY + barHeight,
                    COLOR_DASH_BG
            );

            // Прогресс
            float progress = 1.0F -
                    ((float) data.getDashCooldown()
                            / CombatData.DASH_MAX_COOLDOWN);

            int filled = (int) (barWidth * progress);

            if (filled > 0) {
                gui.fill(
                        barX,
                        barY,
                        barX + filled,
                        barY + barHeight,
                        COLOR_DASH_FILL
                );
            }

            // Текст справа
            gui.drawString(
                    mc.font,
                    "ДЭШ",
                    barX + barWidth + 4,
                    barY - 2,
                    COLOR_DASH_FILL,
                    false
            );
        }

        // ─────────────────────────────────────────────────────────
        // Здесь можно добавить:
        //  - stamina bar
        //  - combo counter
        //  - texture icons
        // ─────────────────────────────────────────────────────────
    }
}