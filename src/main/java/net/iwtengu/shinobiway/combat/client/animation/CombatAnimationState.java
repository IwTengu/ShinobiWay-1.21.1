package net.iwtengu.shinobiway.combat.client.animation;

import net.iwtengu.shinobiway.animation.AnimationDefinition;
import net.iwtengu.shinobiway.animation.ModAnimations;

/**
 * ============================================================
 *  CombatAnimationState — все состояния анимации боевого режима.
 * ============================================================
 *
 * Порядок аргументов СТРОГО совпадает с WeaponGroup.index:
 *   EMPTY  = 0
 *   KUNAI  = 1
 *   KATANA = 2
 *
 * ATTACK_1/2/3 — три удара комбо. Не выбираются в resolveState(),
 * запускаются напрямую через CombatAnimationTicker.playAttack().
 *
 * ---- Добавление нового оружия ------------------------------
 *   Добавь аргумент во ВСЕ состояния (теперь 10 мест с атаками).
 */
public enum CombatAnimationState {

    //                       EMPTY                      KUNAI                       KATANA
    IDLE     (ModAnimations.EMPTY_IDLE,       ModAnimations.KUNAI_IDLE,       ModAnimations.KATANA_IDLE),
    WALK     (ModAnimations.EMPTY_WALK,       ModAnimations.KUNAI_WALK,       ModAnimations.KATANA_WALK),
    WALK_BACK(ModAnimations.EMPTY_WALK_BACK,  ModAnimations.KUNAI_WALK_BACK,  ModAnimations.KATANA_WALK_BACK),
    SNEAK    (ModAnimations.EMPTY_SHIFT,      ModAnimations.KUNAI_SHIFT,      ModAnimations.KATANA_SHIFT),
    RUN      (ModAnimations.EMPTY_RUN,        ModAnimations.KUNAI_RUN,        ModAnimations.KATANA_RUN),
    JUMP     (ModAnimations.EMPTY_JUMP,       ModAnimations.KUNAI_JUMP,       ModAnimations.KATANA_JUMP),
    DASH     (ModAnimations.EMPTY_DASH,       ModAnimations.KUNAI_DASH,       ModAnimations.KATANA_DASH),

    // ── Комбо атак: 3 удара × группа оружия ──────────────────────
    // looping=false — каждый удар играет один раз
    ATTACK_1 (ModAnimations.EMPTY_ATTACK_1,   ModAnimations.KUNAI_ATTACK_1,   ModAnimations.KATANA_ATTACK_1),
    ATTACK_2 (ModAnimations.EMPTY_ATTACK_2,   ModAnimations.KUNAI_ATTACK_2,   ModAnimations.KATANA_ATTACK_2),
    ATTACK_3 (ModAnimations.EMPTY_ATTACK_3,   ModAnimations.KUNAI_ATTACK_3,   ModAnimations.KATANA_ATTACK_3);

    /** Массив анимаций — animations[group.index] */
    private final AnimationDefinition[] animations;

    CombatAnimationState(AnimationDefinition... animations) {
        this.animations = animations;
    }

    public AnimationDefinition getAnimation(CombatWeaponRegistry.WeaponGroup group) {
        int idx = group.index;
        if (idx >= animations.length) return animations[0];
        return animations[idx];
    }

    /**
     * Получить анимацию по числовому индексу группы.
     * Используется в PlayAttackAnimationPayload где приходит groupIndex (int).
     */
    public AnimationDefinition getAnimationByGroupIndex(int groupIndex) {
        if (groupIndex < 0 || groupIndex >= animations.length) return animations[0];
        return animations[groupIndex];
    }

    /**
     * Получить состояние атаки по номеру удара (0, 1, 2).
     * Возвращает ATTACK_1 / ATTACK_2 / ATTACK_3.
     */
    public static CombatAnimationState attackByIndex(int hitIndex) {
        return switch (hitIndex) {
            case 1  -> ATTACK_2;
            case 2  -> ATTACK_3;
            default -> ATTACK_1;
        };
    }
}