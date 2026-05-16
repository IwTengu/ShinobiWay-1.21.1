package net.iwtengu.shinobiway.animation;

import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * ============================================================
 *  AnimationController — главная точка входа для управления
 *  анимациями игрока.
 * ============================================================
 *
 * Этот класс — то, что ты будешь использовать ВЕЗДЕ в моде.
 * Достаточно двух строк чтобы включить или выключить анимацию.
 *
 * Примеры использования:
 * <pre>
 *   // Запустить анимацию (только на клиенте):
 *   AnimationController.play(player, ModAnimations.ABILITY_CAST);
 *
 *   // Остановить анимацию:
 *   AnimationController.stop(player, ModAnimations.ABILITY_CAST);
 *
 *   // Остановить все анимации игрока:
 *   AnimationController.stopAll(player);
 *
 *   // Проверить, играет ли анимация:
 *   if (AnimationController.isPlaying(player, ModAnimations.ABILITY_CHARGE)) { ... }
 *
 *   // Заменить анимацию (стоп + старт):
 *   AnimationController.replace(player, ModAnimations.ATTACK_LIGHT, ModAnimations.ATTACK_HEAVY);
 * </pre>
 *
 * СЕТЬ:
 *   Методы play/stop/stopAll сами отправляют пакет на сервер,
 *   который потом бродкастит его другим клиентам, — поэтому анимации
 *   видят все игроки в онлайне.
 *   Если ты вызываешь метод УЖЕ на сервере, используй overload с
 *   явным флагом side.
 *
 * КЛИЕНТ vs СЕРВЕР:
 *   Сами слои PlayerAnimator существуют только на клиенте.
 *   AnimationController можно вызывать с любой стороны — он сам
 *   разберётся что делать.
 */
public final class AnimationController {

    // Запрет инстанцирования
    private AnimationController() {}

    // =================================================================
    //  PLAY — запуск анимации
    // =================================================================

    /**
     * Запускает анимацию для игрока.
     *
     * <p>Если вызывается на клиенте — применяет анимацию локально
     * И отправляет пакет на сервер (для рассылки другим игрокам).</p>
     * <p>Если вызывается на сервере — отправляет пакет всем клиентам,
     * у которых этот игрок в зоне видимости.</p>
     *
     * @param player игрок (любой стороны)
     * @param def    дескриптор анимации из {@link ModAnimations}
     */
    public static void play(Player player, AnimationDefinition def) {
        if (isClientSide(player)) {
            // Клиент: применить локально
            playLocal(player, def);
            // Клиент: уведомить сервер (сервер разошлёт другим)
            AnimationPacketHandler.sendPlayToServer(def);
        } else {
            // Сервер: разослать пакет всем ближайшим клиентам
            AnimationPacketHandler.broadcastPlay(player, def);
        }
    }

    /**
     * Применяет анимацию только локально (только клиент).
     * Используй, когда уже обрабатываешь входящий сетевой пакет,
     * чтобы не создавать повторную рассылку.
     *
     * @param player игрок
     * @param def    дескриптор анимации
     */
    @OnlyIn(Dist.CLIENT)
    public static void playLocal(Player player, AnimationDefinition def) {
        PlayerAnimationState state = PlayerAnimationManager.get(player);
        if (state == null) return;
        state.play(def);
    }

    // =================================================================
    //  STOP — остановка анимации
    // =================================================================

    /**
     * Останавливает конкретную анимацию.
     * Логика клиент/сервер аналогична {@link #play}.
     *
     * @param player игрок
     * @param def    дескриптор анимации
     */
    public static void stop(Player player, AnimationDefinition def) {
        if (isClientSide(player)) {
            stopLocal(player, def);
            AnimationPacketHandler.sendStopToServer(def);
        } else {
            AnimationPacketHandler.broadcastStop(player, def);
        }
    }

    /**
     * Останавливает анимацию только локально.
     *
     * @param player игрок
     * @param def    дескриптор анимации
     */
    @OnlyIn(Dist.CLIENT)
    public static void stopLocal(Player player, AnimationDefinition def) {
        PlayerAnimationState state = PlayerAnimationManager.get(player);
        if (state == null) return;
        state.stop(def);
    }

    // =================================================================
    //  STOP ALL — остановка всех анимаций
    // =================================================================

    /**
     * Останавливает ВСЕ анимации игрока.
     * Полезно при смерти, телепортации, сбросе состояния.
     *
     * @param player игрок
     */
    public static void stopAll(Player player) {
        if (isClientSide(player)) {
            stopAllLocal(player);
            AnimationPacketHandler.sendStopAllToServer();
        } else {
            AnimationPacketHandler.broadcastStopAll(player);
        }
    }

    /** Останавливает все анимации только локально. */
    @OnlyIn(Dist.CLIENT)
    public static void stopAllLocal(Player player) {
        PlayerAnimationState state = PlayerAnimationManager.get(player);
        if (state == null) return;
        state.stopAll();
    }

    // =================================================================
    //  REPLACE — атомарная замена анимации
    // =================================================================

    /**
     * Останавливает одну анимацию и сразу запускает другую.
     * Удобно для смены фазы: зарядка → выстрел и т.п.
     *
     * @param player  игрок
     * @param oldDef  анимация которую нужно остановить
     * @param newDef  анимация которую нужно начать
     */
    public static void replace(Player player, AnimationDefinition oldDef, AnimationDefinition newDef) {
        stop(player, oldDef);
        play(player, newDef);
    }

    // =================================================================
    //  IS PLAYING — проверка состояния
    // =================================================================

    /**
     * Возвращает true если данная анимация сейчас активна у игрока.
     * Работает только на клиенте — на сервере всегда вернёт false.
     *
     * @param player игрок
     * @param def    дескриптор анимации
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean isPlaying(Player player, AnimationDefinition def) {
        PlayerAnimationState state = PlayerAnimationManager.get(player.getUUID());
        if (state == null) return false;
        return state.isPlaying(def);
    }

    // =================================================================
    //  ВСПОМОГАТЕЛЬНЫЕ
    // =================================================================

    /**
     * Определяет, находимся ли мы на клиентской стороне для данного игрока.
     * Используем level.isClientSide — это надёжнее, чем FMLEnvironment.
     */
    private static boolean isClientSide(Player player) {
        return player.level().isClientSide();
    }
}