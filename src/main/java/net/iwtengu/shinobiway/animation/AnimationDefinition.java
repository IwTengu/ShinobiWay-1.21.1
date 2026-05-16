package net.iwtengu.shinobiway.animation;

import net.minecraft.resources.ResourceLocation;

/**
 * ============================================================
 *  AnimationDefinition — дескриптор (описание) одной анимации.
 * ============================================================
 *
 * Хранит всё необходимое для идентификации и воспроизведения
 * одной анимации:
 *   - ResourceLocation (путь к JSON-файлу в ресурс-паке)
 *   - приоритет (чем выше число, тем «главнее» анимация)
 *   - флаг зацикливания
 *   - скорость воспроизведения (1.0 = нормальная)
 *   - ease (плавность; пока используется как метаданные —
 *     PlayerAnimator берёт ease из самого JSON)
 *
 * Как создавать:
 * <pre>
 *   public static final AnimationDefinition MY_ANIM =
 *       AnimationDefinition.builder("yourmod", "my_attack")
 *           .priority(10)
 *           .looping(false)
 *           .speed(1.0f)
 *           .build();
 * </pre>
 *
 * Регистрация происходит в {@link ModAnimations}.
 */
public final class AnimationDefinition {

    // -----------------------------------------------------------------
    // Поля
    // -----------------------------------------------------------------

    /** Путь вида assets/<namespace>/player_animation/<path>.json */
    private final ResourceLocation location;

    /**
     * Приоритет анимации в слоях.
     * Чем выше значение — тем важнее анимация (перекрывает остальные).
     * Рекомендуемые диапазоны:
     *   0  — idle / фоновые
     *   5  — передвижение
     *   10 — способности
     *   20 — комбо / удары
     *   50 — кат-сцены / перегружающие всё
     */
    private final int priority;

    /** Зациклить анимацию? false = играет один раз и останавливается. */
    private final boolean looping;

    /** Множитель скорости воспроизведения. 1.0 = нормальная скорость. */
    private final float speed;

    // -----------------------------------------------------------------
    // Конструктор (приватный — используй builder)
    // -----------------------------------------------------------------

    private AnimationDefinition(Builder b) {
        this.location = b.location;
        this.priority = b.priority;
        this.looping  = b.looping;
        this.speed    = b.speed;
    }

    // -----------------------------------------------------------------
    // Геттеры
    // -----------------------------------------------------------------

    public ResourceLocation getLocation() { return location; }
    public int getPriority()              { return priority;  }
    public boolean isLooping()            { return looping;   }
    public float getSpeed()               { return speed;     }

    // -----------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------

    /**
     * Начать строить дескриптор анимации.
     *
     * @param namespace  ID мода (например "yourmod")
     * @param animName   имя файла без расширения (например "my_attack")
     *                   Файл должен лежать по пути:
     *                   assets/<namespace>/player_animation/<animName>.json
     */
    public static Builder builder(String namespace, String animName) {
        return new Builder(ResourceLocation.fromNamespaceAndPath(namespace, animName));
    }

    /** Удобная перегрузка, если ResourceLocation уже готов */
    public static Builder builder(ResourceLocation location) {
        return new Builder(location);
    }

    // -----------------------------------------------------------------
    // Внутренний класс Builder
    // -----------------------------------------------------------------

    public static final class Builder {
        private final ResourceLocation location;
        private int   priority = 0;
        private boolean looping = false;
        private float speed    = 1.0f;

        private Builder(ResourceLocation location) {
            this.location = location;
        }

        /** Установить приоритет (см. диапазоны в описании класса). */
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        /** Зациклить ли анимацию. */
        public Builder looping(boolean looping) {
            this.looping = looping;
            return this;
        }

        /** Скорость воспроизведения (1.0 = нормальная). */
        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public AnimationDefinition build() {
            return new AnimationDefinition(this);
        }
    }

    // -----------------------------------------------------------------
    // toString — удобно для отладки
    // -----------------------------------------------------------------

    @Override
    public String toString() {
        return "AnimationDefinition{" +
                "location=" + location +
                ", priority=" + priority +
                ", looping=" + looping +
                ", speed=" + speed +
                '}';
    }
}
