package net.iwtengu.shinobiway.animation;

/**
 * ============================================================
 *  ModAnimations — единое место регистрации всех анимаций.
 * ============================================================
 */
public final class ModAnimations {

    private ModAnimations() {}

    // =============================================================
    //  БОЕВОЙ РЕЖИМ — ПУСТАЯ РУКА
    // =============================================================

    public static final AnimationDefinition EMPTY_IDLE =
            AnimationDefinition.builder("shinobiway", "empty_idle")
                    .priority(0).looping(true).speed(1.0f).build();

    public static final AnimationDefinition EMPTY_WALK =
            AnimationDefinition.builder("shinobiway", "empty_walk")
                    .priority(2).looping(true).speed(1.0f).build();

    public static final AnimationDefinition EMPTY_WALK_BACK =
            AnimationDefinition.builder("shinobiway", "empty_walk_back")
                    .priority(2).looping(true).speed(1.0f).build();

    public static final AnimationDefinition EMPTY_RUN =
            AnimationDefinition.builder("shinobiway", "empty_run")
                    .priority(3).looping(true).speed(1.0f).build();

    public static final AnimationDefinition EMPTY_SHIFT =
            AnimationDefinition.builder("shinobiway", "empty_shift")
                    .priority(2).looping(true).speed(1.0f).build();

    public static final AnimationDefinition EMPTY_JUMP =
            AnimationDefinition.builder("shinobiway", "empty_jump")
                    .priority(4).looping(false).speed(1.0f).build();

    public static final AnimationDefinition EMPTY_DASH =
            AnimationDefinition.builder("shinobiway", "empty_dash")
                    .priority(5).looping(false).speed(1.0f).build();

    // Три удара комбо без оружия. priority=6 — выше дэша (5).
    public static final AnimationDefinition EMPTY_ATTACK_1 =
            AnimationDefinition.builder("shinobiway", "empty_attack_1")
                    .priority(6).looping(false).speed(1.0f).build();

    public static final AnimationDefinition EMPTY_ATTACK_2 =
            AnimationDefinition.builder("shinobiway", "empty_attack_2")
                    .priority(6).looping(false).speed(1.0f).build();

    public static final AnimationDefinition EMPTY_ATTACK_3 =
            AnimationDefinition.builder("shinobiway", "empty_attack_3")
                    .priority(6).looping(false).speed(1.0f).build();

    // =============================================================
    //  БОЕВОЙ РЕЖИМ — КУНАЙ
    // =============================================================

    public static final AnimationDefinition KUNAI_IDLE =
            AnimationDefinition.builder("shinobiway", "kunai_idle")
                    .priority(0).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KUNAI_WALK =
            AnimationDefinition.builder("shinobiway", "kunai_walk")
                    .priority(2).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KUNAI_WALK_BACK =
            AnimationDefinition.builder("shinobiway", "kunai_walk_back")
                    .priority(2).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KUNAI_RUN =
            AnimationDefinition.builder("shinobiway", "kunai_run")
                    .priority(3).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KUNAI_SHIFT =
            AnimationDefinition.builder("shinobiway", "kunai_shift")
                    .priority(2).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KUNAI_JUMP =
            AnimationDefinition.builder("shinobiway", "kunai_jump")
                    .priority(4).looping(false).speed(1.0f).build();

    public static final AnimationDefinition KUNAI_DASH =
            AnimationDefinition.builder("shinobiway", "kunai_dash")
                    .priority(5).looping(false).speed(1.0f).build();

    public static final AnimationDefinition KUNAI_ATTACK_1 =
            AnimationDefinition.builder("shinobiway", "kunai_attack_1")
                    .priority(6).looping(false).speed(1.0f).build();

    public static final AnimationDefinition KUNAI_ATTACK_2 =
            AnimationDefinition.builder("shinobiway", "kunai_attack_2")
                    .priority(6).looping(false).speed(1.0f).build();

    public static final AnimationDefinition KUNAI_ATTACK_3 =
            AnimationDefinition.builder("shinobiway", "kunai_attack_3")
                    .priority(6).looping(false).speed(1.0f).build();

    // =============================================================
    //  БОЕВОЙ РЕЖИМ — КАТАНА
    // =============================================================

    public static final AnimationDefinition KATANA_IDLE =
            AnimationDefinition.builder("shinobiway", "katana_idle")
                    .priority(0).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KATANA_WALK =
            AnimationDefinition.builder("shinobiway", "katana_walk")
                    .priority(2).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KATANA_WALK_BACK =
            AnimationDefinition.builder("shinobiway", "katana_walk_back")
                    .priority(2).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KATANA_RUN =
            AnimationDefinition.builder("shinobiway", "katana_run")
                    .priority(3).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KATANA_SHIFT =
            AnimationDefinition.builder("shinobiway", "katana_shift")
                    .priority(2).looping(true).speed(1.0f).build();

    public static final AnimationDefinition KATANA_JUMP =
            AnimationDefinition.builder("shinobiway", "katana_jump")
                    .priority(4).looping(false).speed(1.0f).build();

    public static final AnimationDefinition KATANA_DASH =
            AnimationDefinition.builder("shinobiway", "katana_dash")
                    .priority(5).looping(false).speed(1.0f).build();

    public static final AnimationDefinition KATANA_ATTACK_1 =
            AnimationDefinition.builder("shinobiway", "katana_attack_1")
                    .priority(6).looping(false).speed(1.0f).build();

    public static final AnimationDefinition KATANA_ATTACK_2 =
            AnimationDefinition.builder("shinobiway", "katana_attack_2")
                    .priority(6).looping(false).speed(1.0f).build();

    public static final AnimationDefinition KATANA_ATTACK_3 =
            AnimationDefinition.builder("shinobiway", "katana_attack_3")
                    .priority(6).looping(false).speed(1.0f).build();

    // =============================================================
    //  ОСОБЫЕ
    // =============================================================

    public static final AnimationDefinition MEDITATION =
            AnimationDefinition.builder("shinobiway", "chakra_meditation")
                    .priority(10000).looping(true).speed(1.0f).build();

    // =============================================================
    //  ДОБАВЛЯЙ НОВОЕ ОРУЖИЕ ПО АНАЛОГИИ — 10 анимаций на группу:
    //  IDLE, WALK, WALK_BACK, RUN, SHIFT, JUMP, DASH,
    //  ATTACK_1, ATTACK_2, ATTACK_3
    // =============================================================
}