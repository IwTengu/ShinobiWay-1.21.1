package net.iwtengu.shinobiway.animation;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 *  PlayerAnimationState — клиентское состояние анимаций игрока.
 * ============================================================
 *
 * Хранит ссылки на ModifierLayer'ы для одного конкретного игрока
 * и предоставляет методы play / stop / isPlaying.
 *
 * ─── ИСПРАВЛЕНИЯ относительно предыдущей версии ─────────────
 *
 *  ❌ PlayerAnimationAccess.getPlayerAnimator()  — МЕТОДА НЕТ
 *  ✅ PlayerAnimationAccess.getPlayerAssociatedData(player).get(key)
 *
 *  ❌ KeyframeAnimationPlayer.isStopped()        — МЕТОДА НЕТ
 *  ✅ layer.getAnimation() != null               — проверка активности
 *
 *  ❌ stack.addAnimLayer(priority, layer) вручную в конструкторе
 *  ✅ PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory()
 *     — регистрируется ОДИН РАЗ при ClientSetup, PlayerAnimator сам
 *       добавляет слой в стек каждого нового игрока автоматически.
 *
 * ─── ПОРЯДОК ИНИЦИАЛИЗАЦИИ ──────────────────────────────────
 *  1. В ClientSetup вызвать: PlayerAnimationState.registerFactories()
 *  2. Получать состояние: PlayerAnimationState.of(player)
 *  3. Управлять: state.play(def) / state.stop(def)
 */
public class PlayerAnimationState {

    // Префикс для ключей AssociatedData, чтобы не пересекаться с другими модами
    private static final String KEY_PREFIX = "anim_";

    // -----------------------------------------------------------------
    // Поля
    // -----------------------------------------------------------------

    private final AbstractClientPlayer player;

    /**
     * Кэш: ResourceLocation анимации → ModifierLayer.
     * Слои создаются фабрикой при появлении игрока в мире.
     * Мы кэшируем их здесь чтобы не обращаться к AssociatedData каждый тик.
     */
    private final Map<ResourceLocation, ModifierLayer<IAnimation>> layerCache = new HashMap<>();

    // -----------------------------------------------------------------
    // Конструктор (приватный)
    // -----------------------------------------------------------------

    private PlayerAnimationState(AbstractClientPlayer player) {
        this.player = player;
        // Заполняем кэш — слои уже созданы фабрикой для этого игрока
        for (AnimationDefinition def : PlayerAnimationManager.getAllDefinitions()) {
            ResourceLocation dataKey = makeDataKey(def);
            @SuppressWarnings("unchecked")
            ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>)
                    PlayerAnimationAccess.getPlayerAssociatedData(player).get(dataKey);
            if (layer != null) {
                layerCache.put(def.getLocation(), layer);
            }
        }
    }

    /** Внутренний фабричный метод — вызывается из PlayerAnimationManager */
    static PlayerAnimationState create(AbstractClientPlayer player) {
        return new PlayerAnimationState(player);
    }

    // -----------------------------------------------------------------
    // РЕГИСТРАЦИЯ ФАБРИК (вызвать ОДИН РАЗ при ClientSetup)
    // -----------------------------------------------------------------

    /**
     * Регистрирует фабрики слоёв для всех анимаций из {@link ModAnimations}.
     *
     * PlayerAnimator вызовет зарегистрированный колбэк для каждого нового
     * игрока и автоматически добавит созданный ModifierLayer в его AnimationStack
     * с указанным приоритетом.
     *
     * Вызывать строго один раз:
     * <pre>
     *   // В методе onClientSetup (modBus-событие):
     *   PlayerAnimationState.registerFactories();
     * </pre>
     */
    public static void registerFactories() {
        for (AnimationDefinition def : PlayerAnimationManager.getAllDefinitions()) {
            ResourceLocation dataKey = makeDataKey(def);
            int priority = def.getPriority();

            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    dataKey,
                    priority,
                    (AbstractClientPlayer player) -> {
                        // Создаём пустой ModifierLayer (null-safe для анимации)
                        ModifierLayer<IAnimation> layer = new ModifierLayer<>();
                        // Сохраняем в AssociatedData игрока — PlayerAnimator добавит
                        // его в AnimationStack с нашим приоритетом автоматически
                        PlayerAnimationAccess.getPlayerAssociatedData(player).set(dataKey, layer);
                        return layer;
                    }
            );
        }
    }

    // -----------------------------------------------------------------
    // ВОСПРОИЗВЕДЕНИЕ
    // -----------------------------------------------------------------

    /**
     * Запускает анимацию. Если уже играет — перезапускает с начала.
     *
     * @param def дескриптор из {@link ModAnimations}
     */
    public void play(AnimationDefinition def) {
        ModifierLayer<IAnimation> layer = getLayer(def);
        if (layer == null) return;

        var keyframeAnim = PlayerAnimationRegistry.getAnimation(def.getLocation());
        if (keyframeAnim == null) {
            System.err.println("[AnimSystem] Animation not found: " + def.getLocation()
                    + "  →  Ожидается файл: assets/"
                    + def.getLocation().getNamespace()
                    + "/player_animation/"
                    + def.getLocation().getPath() + ".json");
            return;
        }

        // Создаём KeyframeAnimationPlayer
        // Конструктор: KeyframeAnimationPlayer(KeyframeAnimation animation)
        // Скорость (если не 1.0) — применяем через extraTick.
        // extraTick = 0 → нормальная скорость.
        // Альтернатива для скорости: использовать SpeedModifier через ModifierLayer.
        KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(keyframeAnim);

        // Устанавливаем в слой — PlayerAnimator начнёт рендерить анимацию
        layer.setAnimation(animPlayer);
    }

    // -----------------------------------------------------------------
    // ОСТАНОВКА
    // -----------------------------------------------------------------

    /**
     * Останавливает анимацию. Передача null в setAnimation безопасна —
     * ModifierLayer становится «прозрачным» (не влияет на позу игрока).
     *
     * @param def дескриптор анимации
     */
    public void stop(AnimationDefinition def) {
        ModifierLayer<IAnimation> layer = getLayer(def);
        if (layer == null) return;
        layer.setAnimation(null);
    }

    /**
     * Останавливает ВСЕ анимации игрока.
     */
    public void stopAll() {
        for (ModifierLayer<IAnimation> layer : layerCache.values()) {
            layer.setAnimation(null);
        }
    }

    // -----------------------------------------------------------------
    // ЗАПРОС СОСТОЯНИЯ
    // -----------------------------------------------------------------

    /**
     * Возвращает true если анимация сейчас активна.
     *
     * В API 2.0 метода isStopped() нет. Признак активности —
     * layer.getAnimation() != null. Когда однократная анимация
     * (не looping) завершается, PlayerAnimator автоматически
     * обнуляет её внутри ModifierLayer.
     *
     * @param def дескриптор анимации
     */
    public boolean isPlaying(AnimationDefinition def) {
        ModifierLayer<IAnimation> layer = getLayer(def);
        if (layer == null) return false;
        return layer.getAnimation() != null;
    }

    // -----------------------------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ
    // -----------------------------------------------------------------

    /**
     * Возвращает слой из кэша или «лениво» достаёт из AssociatedData.
     * Нужен lazy-путь на случай если State создан раньше, чем
     * фабрика успела заполнить AssociatedData (редкая гонка при загрузке).
     */
    @SuppressWarnings("unchecked")
    private ModifierLayer<IAnimation> getLayer(AnimationDefinition def) {
        ModifierLayer<IAnimation> cached = layerCache.get(def.getLocation());
        if (cached != null) return cached;

        // Lazy fetch из AssociatedData
        ResourceLocation dataKey = makeDataKey(def);
        ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>)
                PlayerAnimationAccess.getPlayerAssociatedData(player).get(dataKey);
        if (layer != null) {
            layerCache.put(def.getLocation(), layer);
        }
        return layer;
    }

    /**
     * Строит ключ AssociatedData по дескриптору.
     * "yourmod:ability_cast" → "yourmod:anim_ability_cast"
     */
    private static ResourceLocation makeDataKey(AnimationDefinition def) {
        return ResourceLocation.fromNamespaceAndPath(
                def.getLocation().getNamespace(),
                KEY_PREFIX + def.getLocation().getPath()
        );
    }

    public AbstractClientPlayer getPlayer() {
        return player;
    }
}