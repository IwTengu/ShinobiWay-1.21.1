package net.iwtengu.shinobiway.client;

import net.iwtengu.shinobiway.entity.custom.SeatEntity;
import net.iwtengu.shinobiway.network.MeditationSuccessPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

@EventBusSubscriber(modid = "shinobiway", value = Dist.CLIENT)
public class MeditationOverlay {

    private static final Random RANDOM = new Random();

    private static boolean onCarpet      = false;
    private static boolean circleVisible = false;
    private static int circleTimer       = 0;
    private static int nextCircle        = 0;

    private static final int CIRCLE_DURATION = 20; // 1 секунда

    // ── Рендер ────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean sitting = mc.player.getVehicle() instanceof SeatEntity;

        // Сел на ковёр
        if (sitting && !onCarpet) {
            onCarpet      = true;
            circleVisible = false;
            circleTimer   = 0;
            nextCircle    = randomInterval();
        }

        // Встал с ковра
        if (!sitting && onCarpet) {
            onCarpet      = false;
            circleVisible = false;
        }

        if (!onCarpet) return;

        GuiGraphics gui = event.getGuiGraphics();
        int w  = mc.getWindow().getGuiScaledWidth();
        int h  = mc.getWindow().getGuiScaledHeight();
        int cx = w / 2;
        int cy = h / 2;

        // Инструкция сверху
        gui.drawCenteredString(
                mc.font,
                Component.literal("Медитация: нажми ПРОБЕЛ когда появится красный круг"),
                cx, 20, 0xFFFFFF
        );

        // Обновляем таймеры
        if (circleVisible) {
            circleTimer--;
            if (circleTimer <= 0) {
                circleVisible = false;
                nextCircle    = randomInterval();
            }
        } else {
            nextCircle--;
            if (nextCircle <= 0) {
                circleVisible = true;
                circleTimer   = CIRCLE_DURATION;
            }
        }

        // Рисуем круг
        if (circleVisible) {
            drawFilledCircle(gui, cx, cy, 20, 0xFFFF0000);
        }
    }

    // ── Обработка пробела ─────────────────────────────────────────────────

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {

        if (!onCarpet) return;
        if (event.getKey() != GLFW.GLFW_KEY_SPACE) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        if (circleVisible) {
            PacketDistributor.sendToServer(new MeditationSuccessPayload());
            circleVisible = false;
            nextCircle    = randomInterval();
        }
    }

    // ── Утилиты ───────────────────────────────────────────────────────────

    /** От 10 до 60 секунд в тиках */
    private static int randomInterval() {
        return (3 + RANDOM.nextInt(8)) * 20;
    }

    private static void drawFilledCircle(GuiGraphics gui, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt((double)(radius * radius - dy * dy));
            gui.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }
}
