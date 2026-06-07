package net.iwtengu.shinobiway.combat;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                        CombatData                           ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class CombatData {

    // ── Разблокировка ─────────────────────────────────────────────
    private boolean unlocked = false;

    // ── Состояние ─────────────────────────────────────────────────
    private boolean active = false;

    // ── Дэш ───────────────────────────────────────────────────────
    private int dashCooldown = 0;
    public static final int DASH_MAX_COOLDOWN = 20;

    // ── Атака / Комбо ─────────────────────────────────────────────
    private int attackCooldown = 0;
    private int comboStep = 0;
    private int comboResetTimer = 0;
    public static final int COMBO_RESET_TICKS = 20;

    // ── Окно столкновения ─────────────────────────────────────────
    /**
     * Сколько тиков осталось в "активном окне" удара.
     * Пока > 0 — удар игрока считается активным и может столкнуться
     * с ударом другого игрока.
     *
     * Окно открывается в AttackRequestPayload.handle() сразу
     * при регистрации удара. Убывает каждый тик в CombatServerEventHandler.
     *
     * Значение устанавливается через CombatAttackRegistry.getClashWindow().
     */
    private int clashWindow = 0;

    /**
     * Группа оружия в момент текущего удара.
     * Нужна ClashDetector'у чтобы знать с чем столкнулись.
     * Хранится как int (WeaponGroup.index) чтобы не тянуть
     * клиентский класс на сервер.
     */
    private int clashWeaponIndex = 0;

    // ── Геттеры / Сеттеры ─────────────────────────────────────────

    public boolean isUnlocked()           { return unlocked; }
    public void    setUnlocked(boolean v) { unlocked = v; }

    public boolean isActive()             { return active; }
    public void    setActive(boolean v)   { active = v; }

    public int  getDashCooldown()         { return dashCooldown; }
    public void setDashCooldown(int v)    { dashCooldown = Math.max(0, v); }
    public void tickDashCooldown()        { if (dashCooldown > 0) dashCooldown--; }
    public boolean canDash()              { return dashCooldown == 0; }

    public int  getAttackCooldown()       { return attackCooldown; }
    public void setAttackCooldown(int v)  { attackCooldown = Math.max(0, v); }
    public void tickAttackCooldown()      { if (attackCooldown > 0) attackCooldown--; }
    public boolean canAttack()            { return attackCooldown == 0; }

    public int getComboStep() { return comboStep; }

    public int advanceCombo() {
        int current = comboStep;
        comboStep = (comboStep + 1) % 3;
        comboResetTimer = COMBO_RESET_TICKS;
        return current;
    }

    public void resetCombo() {
        comboStep = 0;
        comboResetTimer = 0;
    }

    public void tickComboReset() {
        if (comboResetTimer > 0) {
            comboResetTimer--;
            if (comboResetTimer == 0) comboStep = 0;
        }
    }

    // ── Окно столкновения ─────────────────────────────────────────

    /** Открыть окно столкновения */
    public void openClashWindow(int ticks, int weaponIndex) {
        this.clashWindow = ticks;
        this.clashWeaponIndex = weaponIndex;
    }

    /** Закрыть окно (удар уже обработан или истёк) */
    public void closeClashWindow() {
        clashWindow = 0;
        clashWeaponIndex = 0;
    }

    public void tickClashWindow() {
        if (clashWindow > 0) clashWindow--;
    }

    /** true — игрок сейчас находится в активном окне удара */
    public boolean hasClashWindow() { return clashWindow > 0; }

    /** Сколько тиков осталось в окне столкновения */
    public int getClashWindowRemaining() { return clashWindow; }

    public int getClashWeaponIndex() { return clashWeaponIndex; }

    // ── Копирование при смерти ────────────────────────────────────

    public void copyFrom(CombatData other) {
        this.unlocked = other.unlocked;
    }
}