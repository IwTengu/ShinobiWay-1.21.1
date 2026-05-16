package net.iwtengu.shinobiway.combat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                    CombatKeyBindings                        ║
 * ║  Клавиша B — переключение боевого режима.                   ║
 * ║  Регистрируется через RegisterKeyMappingsEvent на mod bus.  ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Добавление новой клавиши:
 *  1. Объяви static final KeyMapping здесь
 *  2. Зарегистрируй в onRegisterKeyMappings()
 *  3. Обрабатывай в CombatClientEventHandler.onClientTick()
 *  4. Добавь перевод в en_us.json и ru_ru.json
 */
public class CombatKeyBindings {

    /**
     * Клавиша переключения боевого режима.
     * По умолчанию — B. Игрок может переназначить в настройках.
     */
    public static final KeyMapping TOGGLE_COMBAT = new KeyMapping(
            "key.shinobiway.toggle_combat",       // ключ для перевода
            InputConstants.Type.KEYSYM,            // тип: клавиатура
            GLFW.GLFW_KEY_B,                       // клавиша B
            "key.categories.shinobiway"            // категория в настройках управления
    );

    // ── Место для новых клавиш ─────────────────────────────────
    // public static final KeyMapping SPECIAL_ATTACK = new KeyMapping(
    //         "key.shinobiway.special_attack",
    //         InputConstants.Type.KEYSYM,
    //         GLFW.GLFW_KEY_G,
    //         "key.categories.shinobiway"
    // );

    /**
     * Регистрация в NeoForge.
     * Подключается к RegisterKeyMappingsEvent в ShinobiWayMod (mod bus).
     */
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_COMBAT);
        // event.register(SPECIAL_ATTACK);
    }
}