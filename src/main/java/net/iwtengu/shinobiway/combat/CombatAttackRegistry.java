package net.iwtengu.shinobiway.combat;

import net.iwtengu.shinobiway.combat.client.animation.CombatWeaponRegistry;
import net.iwtengu.shinobiway.combat.client.animation.CombatWeaponRegistry.WeaponGroup;

import java.util.EnumMap;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                   CombatAttackRegistry                      ║
 * ║  Реестр скоростей атаки и окон столкновения по группам.     ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * clashWindow — сколько тиков удар считается "активным" для столкновения.
 * Чем больше — тем проще поймать столкновение.
 * Рекомендуется: 3-5 тиков (0.15 - 0.25 сек).
 *
 * perfectClashWindow — окно "идеального" столкновения (перехват оружия).
 * Должно быть меньше clashWindow. Рекомендуется: 1-2 тика.
 */
public class CombatAttackRegistry {

    private static final int DEFAULT_COOLDOWN      = 10;
    private static final int DEFAULT_CLASH_WINDOW  = 4;
    private static final int DEFAULT_PERFECT_WINDOW = 2;

    private record AttackConfig(int cooldown, int clashWindow, int perfectClashWindow) {}

    private static final Map<WeaponGroup, AttackConfig> CONFIGS = new EnumMap<>(WeaponGroup.class);

    // ─────────────────────────────────────────────────────────
    //  НАСТРОЙКА — заполняй здесь
    // ─────────────────────────────────────────────────────────

    /**
     * Регистрация конфигов атаки.
     * register(группа, кулдаун_тиков, окно_столкновения_тиков, окно_перехвата_тиков)
     *
     * Кулдаун:           сколько тиков между ударами
     * clashWindow:       сколько тиков удар "активен" для столкновения
     * perfectClashWindow: сколько тиков из clashWindow считаются "идеальным" перехватом
     *                    (perfectClashWindow <= clashWindow)
     */
    public static void registerAll() {
        register(WeaponGroup.EMPTY,  10, 5, 3);  // кулак: кд=10, окно=4, перехват=2
        register(WeaponGroup.KUNAI,  8,  5, 3);  // кунай: кд=8,  окно=4, перехват=2
        register(WeaponGroup.KATANA, 15, 5, 3);  // катана: кд=15, окно=5, перехват=2
    }

    public static void register(WeaponGroup group, int cooldown, int clashWindow, int perfectClashWindow) {
        CONFIGS.put(group, new AttackConfig(cooldown, clashWindow, perfectClashWindow));
    }

    // ─────────────────────────────────────────────────────────
    //  API
    // ─────────────────────────────────────────────────────────

    public static int getCooldown(WeaponGroup group) {
        AttackConfig cfg = CONFIGS.get(group);
        return cfg != null ? cfg.cooldown() : DEFAULT_COOLDOWN;
    }

    /** Сколько тиков удар считается активным для столкновения */
    public static int getClashWindow(WeaponGroup group) {
        AttackConfig cfg = CONFIGS.get(group);
        return cfg != null ? cfg.clashWindow() : DEFAULT_CLASH_WINDOW;
    }

    /**
     * Сколько тиков из clashWindow считаются "идеальным" перехватом.
     * В это окно игрок без оружия может отобрать оружие у вооружённого.
     */
    public static int getPerfectClashWindow(WeaponGroup group) {
        AttackConfig cfg = CONFIGS.get(group);
        return cfg != null ? cfg.perfectClashWindow() : DEFAULT_PERFECT_WINDOW;
    }

    public static int getCooldownForStack(net.minecraft.world.item.ItemStack stack) {
        return getCooldown(CombatWeaponRegistry.resolveGroup(stack));
    }

    private CombatAttackRegistry() {}
}