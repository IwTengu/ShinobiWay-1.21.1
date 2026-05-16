package net.iwtengu.shinobiway.chakra;

import net.minecraft.nbt.CompoundTag;

/**
 * ╔══════════════════════════════════════════════════════════╗
 *  ChakraData — хранит все данные чакры одного игрока.
 *  Сохраняется в NBT, переживает смерть и рестарт.
 * ╚══════════════════════════════════════════════════════════╝
 *
 *  ── КАК ПОЛУЧИТЬ ОБЪЕКТ ChakraData ──────────────────────────
 *
 *  // На СЕРВЕРЕ (ServerPlayer):
 *  player.getCapability(ChakraCapability.CHAKRA).ifPresent(chakra -> {
 *      // работа с chakra
 *  });
 *
 *  // Сокращённо (если нужно вернуть значение):
 *  ChakraData chakra = player.getCapability(ChakraCapability.CHAKRA).orElse(null);
 *
 * ────────────────────────────────────────────────────────────
 */
public class ChakraData {

    // ── Константы по умолчанию ──────────────────────────────
    public static final float DEFAULT_MAX    = 100f;
    public static final float DEFAULT_AMOUNT = 100f;

    // ── Поля ────────────────────────────────────────────────
    private float current;   // текущая чакра
    private float max;       // максимальный запас
    private boolean unlocked; // разблокирована ли чакра у игрока

    // ── Конструктор ─────────────────────────────────────────
    public ChakraData() {
        this.current  = DEFAULT_AMOUNT;
        this.max      = DEFAULT_MAX;
        this.unlocked = false; // по умолчанию заблокирована
    }

    // ════════════════════════════════════════════════════════
    //  ГЕТТЕРЫ
    // ════════════════════════════════════════════════════════

    /** Возвращает текущее количество чакры */
    public float getCurrent() { return current; }

    /** Возвращает максимальный запас чакры */
    public float getMax() { return max; }

    /** Возвращает true, если чакра разблокирована у игрока */
    public boolean isUnlocked() { return unlocked; }

    /** Возвращает текущую чакру в процентах от максимума (0.0 – 1.0) */
    public float getPercent() { return (max > 0) ? (current / max) : 0f; }

    // ════════════════════════════════════════════════════════
    //  РАЗБЛОКИРОВКА
    // ════════════════════════════════════════════════════════

    /**
     * ✅ РАЗБЛОКИРОВАТЬ ЧАКРУ у игрока.

     * Вставь в нужный класс:
     *   player.getCapability(ChakraCapability.CHAKRA).ifPresent(ChakraData::unlock);
     *   ChakraSyncPacket.send(player); // не забудь синхронизировать!
     */
    public void unlock() { this.unlocked = true; }

    /** Заблокировать чакру обратно (если нужно) */
    public void lock() { this.unlocked = false; }

    // ════════════════════════════════════════════════════════
    //  ИЗМЕНЕНИЕ ТЕКУЩЕЙ ЧАКРЫ (абсолютные значения)
    // ════════════════════════════════════════════════════════

    /**
     * ➕ Добавить чакру на конкретное значение.
     *
     * Использование:
     *   chakra.add(25f);
     */
    public void add(float amount) {
        current = Math.min(current + amount, max);
    }

    /**
     * ➖ Потратить/убрать чакру на конкретное значение.
     * Возвращает true, если чакры хватило (операция выполнена).
     * Возвращает false, если чакры НЕ хватило (ничего не изменилось).
     *
     * Использование (с проверкой):
     *   if (chakra.spend(30f)) { // заклинание выполнено }
     *   else                   { // недостаточно чакры   }
     *
     * Использование (без проверки, просто уменьшить):
     *   chakra.remove(30f);
     */
    public boolean spend(float amount) {
        if (current >= amount) { current -= amount; return true; }
        return false;
    }

    /**
     * ➖ Убрать чакру (принудительно, даже если уйдёт в 0).
     *
     * Использование:
     *   chakra.remove(30f);
     */
    public void remove(float amount) {
        current = Math.max(0f, current - amount);
    }

    /**
     * 🔧 Установить текущую чакру на конкретное значение.
     *
     * Использование:
     *   chakra.setCurrent(50f);
     */
    public void setCurrent(float value) {
        current = Math.max(0f, Math.min(value, max));
    }

    /** Полностью восстановить чакру до максимума */
    public void fillToMax() { current = max; }

    /** Обнулить текущую чакру */
    public void empty() { current = 0f; }

    // ════════════════════════════════════════════════════════
    //  ИЗМЕНЕНИЕ ТЕКУЩЕЙ ЧАКРЫ (проценты от максимума)
    // ════════════════════════════════════════════════════════

    /**
     * ➕ Добавить чакру на X% от максимума.
     *
     * Использование (восстановить 20% маны):
     *   chakra.addPercent(0.20f);
     */
    public void addPercent(float percent) {
        add(max * percent);
    }

    /**
     * ➖ Убрать чакру на X% от максимума (принудительно).
     *
     * Использование (потратить 15% маны):
     *   chakra.removePercent(0.15f);
     */
    public void removePercent(float percent) {
        remove(max * percent);
    }

    /**
     * ➖ Потратить чакру на X% от максимума (с проверкой остатка).
     * Возвращает true если хватило.
     *
     * Использование:
     *   if (chakra.spendPercent(0.10f)) { // стоит 10% маны }
     */
    public boolean spendPercent(float percent) {
        return spend(max * percent);
    }

    // ════════════════════════════════════════════════════════
    //  ИЗМЕНЕНИЕ МАКСИМАЛЬНОГО ЗАПАСА
    // ════════════════════════════════════════════════════════

    /**
     * ➕ Увеличить максимальный запас чакры.
     *
     * Использование:
     *   chakra.addMax(50f);
     */
    public void addMax(float amount) {
        max += amount;
    }

    /**
     * ➖ Уменьшить максимальный запас чакры (минимум 1).
     *
     * Использование:
     *   chakra.removeMax(20f);
     */
    public void removeMax(float amount) {
        max = Math.max(1f, max - amount);
        current = Math.min(current, max); // текущая не может быть выше макс
    }

    /**
     * 🔧 Установить максимальный запас чакры на конкретное значение.
     *
     * Использование:
     *   chakra.setMax(200f);
     */
    public void setMax(float value) {
        max = Math.max(1f, value);
        current = Math.min(current, max);
    }

    // ════════════════════════════════════════════════════════
    //  NBT — сохранение и загрузка (переживает смерть и рестарт)
    // ════════════════════════════════════════════════════════

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("current",  current);
        tag.putFloat("max",      max);
        tag.putBoolean("unlocked", unlocked);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        current  = tag.getFloat("current");
        max      = tag.getFloat("max");
        unlocked = tag.getBoolean("unlocked");
    }
}