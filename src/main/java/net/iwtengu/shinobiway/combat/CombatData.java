package net.iwtengu.shinobiway.combat;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                        CombatData                           ║
 * ║  POJO — хранит все данные боевого режима одного игрока.     ║
 * ║  Привязывается к игроку через AttachmentType (NeoForge).    ║
 * ║  Регистрация AttachmentType — в CombatAttachments.          ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * NeoForge 1.21.1 использует Data Attachments вместо старых Capability.
 * Данные сериализуются через Codec (задаётся в CombatAttachments).
 *
 * Расширение: добавляй новые поля сюда (стамина, ранг, комбо и т.д.)
 */
public class CombatData {

    // ── Разблокировка ─────────────────────────────────────────────
    /**
     * Открыт ли доступ к боевому режиму.
     * По умолчанию false. Открывается через CombatHelper.unlock().
     */
    private boolean unlocked = false;

    // ── Состояние ─────────────────────────────────────────────────
    /**
     * Активен ли боевой режим прямо сейчас.
     * НЕ сериализуется — сбрасывается при каждом входе в игру.
     */
    private boolean active = false;

    // ── Дэш ───────────────────────────────────────────────────────
    /**
     * Кулдаун дэша в тиках. Убывает на 1 каждый тик.
     * НЕ сериализуется — сбрасывается при входе в игру.
     */
    private int dashCooldown = 0;

    /** Максимальный кулдаун дэша: 10 тиков = 0.5 секунды */
    public static final int DASH_MAX_COOLDOWN = 20;

    // ── Геттеры / Сеттеры ─────────────────────────────────────────

    public boolean isUnlocked()           { return unlocked; }
    public void    setUnlocked(boolean v) { unlocked = v; }

    public boolean isActive()             { return active; }
    public void    setActive(boolean v)   { active = v; }

    public int  getDashCooldown()         { return dashCooldown; }
    public void setDashCooldown(int v)    { dashCooldown = Math.max(0, v); }
    public void tickDashCooldown()        { if (dashCooldown > 0) dashCooldown--; }
    public boolean canDash()              { return dashCooldown == 0; }

    // ── Копирование при смерти ────────────────────────────────────

    /**
     * Копируем данные из старого игрока в нового (при смерти / respawn).
     * Копируем ТОЛЬКО unlocked — active и cooldown сбрасываются.
     */
    public void copyFrom(CombatData other) {
        this.unlocked = other.unlocked;
        // active не копируем — после смерти режим сброшен
    }
}
