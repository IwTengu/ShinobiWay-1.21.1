package net.iwtengu.shinobiway.chakra;

import net.iwtengu.shinobiway.ShinobiWay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * ╔══════════════════════════════════════════════════════════╗
 *  ChakraHudOverlay — рисует шкалу чакры в левом нижнем углу.
 *
 *  Шкала видна только если чакра РАЗБЛОКИРОВАНА.
 *  Показывает: полоску + текст "Чакра: X / MAX"
 *
 *  ── КАК ПОДКЛЮЧИТЬ ──────────────────────────────────────────
 *  Аннотация @EventBusSubscriber(Dist.CLIENT) ниже всё сделает
 *  автоматически — просто убедись, что класс в classpath.
 * ╚══════════════════════════════════════════════════════════╝
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = ShinobiWay.MOD_ID, value = Dist.CLIENT)
public class ChakraHudOverlay {

    // ── Настройки внешнего вида ──────────────────────────────
    private static final int BAR_WIDTH       = 100; // ширина полоски в пикселях
    private static final int BAR_HEIGHT      = 8;   // высота полоски в пикселях
    private static final int MARGIN_LEFT     = 10;  // отступ от левого края
    private static final int MARGIN_BOTTOM   = 10;  // отступ от нижнего края

    // Цвета полоски
    private static final int COLOR_BG        = 0xFF1A1A2E; // тёмный фон
    private static final int COLOR_FILL      = 0xFF4A90D9; // синяя чакра
    private static final int COLOR_FILL_LOW  = 0xFF8B4513; // красный при малом кол-ве (<20%)
    private static final int COLOR_BORDER    = 0xFF6A5ACD; // сине-фиолетовая рамка
    private static final int COLOR_TEXT      = 0xFFCCCCFF; // цвет текста

    // Порог "мало чакры" — полоска меняет цвет
    private static final float LOW_THRESHOLD = 0.20f; // 20%

    /**
     * Отрисовка после стандартного HUD (поверх обычных элементов).
     * Рендерится только на клиенте, только если чакра разблокирована.
     */
    @SubscribeEvent
    public static void onRenderHud(RenderGuiLayerEvent.Post event) {
        // Рендерим только после слоя HOTBAR (после основного HUD)
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // Получаем данные чакры
        ChakraData chakra = ChakraAttachment.get(player);
        if (chakra == null) return;

        // Шкала видна ТОЛЬКО если чакра разблокирована
        if (!chakra.isUnlocked()) return;

        GuiGraphics gui      = event.getGuiGraphics();
        int screenWidth      = mc.getWindow().getGuiScaledWidth();
        int screenHeight     = mc.getWindow().getGuiScaledHeight();

        // ── Позиция в левом нижнем углу ─────────────────────
        int x = MARGIN_LEFT;
        int y = screenHeight - MARGIN_BOTTOM - BAR_HEIGHT - 10; // 10 = место под текст

        float percent = chakra.getPercent(); // 0.0 – 1.0
        int fillWidth = (int) (BAR_WIDTH * percent);

        // ── Рисуем фон полоски ───────────────────────────────
        gui.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BG);

        // ── Рисуем заполнение полоски ────────────────────────
        int fillColor = (percent <= LOW_THRESHOLD) ? COLOR_FILL_LOW : COLOR_FILL;
        if (fillWidth > 0) {
            gui.fill(x, y, x + fillWidth, y + BAR_HEIGHT, fillColor);
        }

        // ── Рисуем рамку (4 линии по 1px) ───────────────────
        gui.fill(x - 1,            y - 1,             x + BAR_WIDTH + 1, y,                  COLOR_BORDER); // верх
        gui.fill(x - 1,            y + BAR_HEIGHT,    x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, COLOR_BORDER); // низ
        gui.fill(x - 1,            y,                 x,                 y + BAR_HEIGHT,     COLOR_BORDER); // лево
        gui.fill(x + BAR_WIDTH,    y,                 x + BAR_WIDTH + 1, y + BAR_HEIGHT,     COLOR_BORDER); // право

        // ── Рисуем текст над полоской ────────────────────────
        String text = "Чакра: " + (int) chakra.getCurrent() + " / " + (int) chakra.getMax();
        gui.drawString(mc.font, text, x, y - 10, COLOR_TEXT, false);
    }
}