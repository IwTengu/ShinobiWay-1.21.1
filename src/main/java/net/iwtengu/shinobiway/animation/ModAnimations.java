package net.iwtengu.shinobiway.animation;

/**
 * ============================================================
 *  ModAnimations — единое место регистрации всех анимаций.
 * ============================================================
 *
 * Здесь ты объявляешь ВСЕ анимации своего мода как статические
 * константы. Никакого кода запуска / воспроизведения здесь нет —
 * только декларация.
 *
 * Пример добавления новой анимации:
 * <pre>
 *   public static final AnimationDefinition MY_NEW_ANIM =
 *       AnimationDefinition.builder("yourmod", "my_new_anim")
 *           .priority(10)
 *           .looping(false)
 *           .speed(1.0f)
 *           .build();
 * </pre>
 *
 * После объявления анимацию можно запустить из любого места мода:
 * <pre>
 *   AnimationController.play(player, ModAnimations.MY_NEW_ANIM);
 *   AnimationController.stop(player, ModAnimations.MY_NEW_ANIM);
 * </pre>
 *
 * Имя файла анимации = второй аргумент builder'а + ".json"
 * Путь в ресурс-паке: assets/yourmod/player_animation/<name>.json
 *
 * ---- ПРИОРИТЕТЫ (рекомендации) --------------------------------
 *  0   Idle / ambient
 *  5   Locomotion (бег, плавание)
 *  10  Способности / умения
 *  20  Атаки / комбо
 *  50  Кат-сцены / overriding-анимации
 * ---------------------------------------------------------------
 */
public final class ModAnimations {

    // Запрет создания экземпляра (утилитный класс)
    private ModAnimations() {}

    // =============================================================
    //  IDLE / AMBIENT
    // =============================================================

    /** Анимация покоя — игрок стоит и ничего не делает */
    public static final AnimationDefinition EMPTY_IDLE =
            AnimationDefinition.builder("shinobiway", "empty_idle")
                    .priority(0)
                    .looping(true)
                    .speed(1.0f)
                    .build();

    /** ход вперёд */
    public static final AnimationDefinition EMPTY_WALK =
            AnimationDefinition.builder("shinobiway", "empty_walk")
                    .priority(2)
                    .looping(true)
                    .speed(1.0f)
                    .build();

    /** Бег вперёд */
    public static final AnimationDefinition EMPTY_RUN =
            AnimationDefinition.builder("shinobiway", "empty_run")
                    .priority(3)
                    .looping(true)
                    .speed(1.0f)
                    .build();

    public static final AnimationDefinition EMPTY_SHIFT =
            AnimationDefinition.builder("shinobiway", "empty_shift")
                    .priority(4)
                    .looping(true)
                    .speed(1.0f)
                    .build();

    // =============================================================
    //  СПОСОБНОСТИ
    // =============================================================

    /**
     * Пример: анимация заряда способности.
     * Зациклена — продолжается, пока игрок держит кнопку.
     */
    public static final AnimationDefinition ABILITY_CHARGE =
            AnimationDefinition.builder("yourmod", "ability_charge")
                    .priority(10)
                    .looping(true)
                    .speed(1.0f)
                    .build();

    /**
     * Пример: анимация выстрела / броска.
     * Не зациклена — играет один раз.
     */
    public static final AnimationDefinition ABILITY_CAST =
            AnimationDefinition.builder("yourmod", "ability_cast")
                    .priority(10)
                    .looping(false)
                    .speed(1.2f)  // немного быстрее для резкости
                    .build();

    // =============================================================
    //  АТАКИ
    // =============================================================

    /** Лёгкий удар */
    public static final AnimationDefinition ATTACK_LIGHT =
            AnimationDefinition.builder("yourmod", "attack_light")
                    .priority(20)
                    .looping(false)
                    .speed(1.0f)
                    .build();

    /** Тяжёлый удар (выше приоритет — перебьёт лёгкий) */
    public static final AnimationDefinition ATTACK_HEAVY =
            AnimationDefinition.builder("yourmod", "attack_heavy")
                    .priority(22)
                    .looping(false)
                    .speed(0.9f)
                    .build();

    // =============================================================
    //  ОСОБЫЕ / КАТ-СЦЕНЫ
    // =============================================================

    /** Анимация победы — перекрывает всё остальное */
    public static final AnimationDefinition MEDITATION =
            AnimationDefinition.builder("shinobiway", "chakra_meditation")
                    .priority(10000)
                    .looping(true)
                    .speed(1.0f)
                    .build();

    // =============================================================
    //  ДОБАВЛЯЙ СВОИ АНИМАЦИИ НИЖЕ ПО АНАЛОГИИ
    // =============================================================
}