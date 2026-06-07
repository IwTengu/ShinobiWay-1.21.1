package net.iwtengu.shinobiway.combat.client.animation;

import net.iwtengu.shinobiway.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * ============================================================
 *  CombatWeaponRegistry — реестр групп оружия.
 * ============================================================
 *
 * WeaponGroup определяет какой набор анимаций использовать.
 * Каждый Item привязывается к одной группе.
 *
 * ---- Как добавить новый вид оружия --------------------------
 *
 *   ШАГ 1: Добавь константу в WeaponGroup с следующим индексом.
 *           index должен совпасть с позицией аргумента
 *           в CombatAnimationState (счёт с 0).
 *
 *   ШАГ 2: Добавь анимации в ModAnimations:
 *           SHURIKEN_IDLE, SHURIKEN_WALK, SHURIKEN_WALK_BACK,
 *           SHURIKEN_RUN, SHURIKEN_SHIFT, SHURIKEN_JUMP, SHURIKEN_DASH
 *
 *   ШАГ 3: Добавь аргумент во ВСЕ состояния CombatAnimationState.
 *
 *   ШАГ 4: Зарегистрируй предмет здесь в registerAll():
 *           register(WeaponGroup.SHURIKEN, ModItems.SHURIKEN.get());
 *
 * ---- Как использовать из другого класса ---------------------
 *
 *   WeaponGroup group = CombatWeaponRegistry.resolveGroup(player.getMainHandItem());
 *   boolean isArmed = group != WeaponGroup.EMPTY;
 */
public class CombatWeaponRegistry {

    /**
     * Группы оружия.
     *
     * index — явное число, используется в CombatAnimationState
     * для выбора нужной анимации из массива.
     *
     * ВАЖНО: не меняй index существующих констант!
     * Новые группы добавляй со следующим по порядку индексом.
     */
    public enum WeaponGroup {
        EMPTY (0),   // пустая рука — не регистрируй сюда предметы
        KUNAI (1),   // кунаи, метательные ножи
        KATANA(2);   // катаны, длинные клинки
        // Пример расширения: SHURIKEN(3), BOW(4), STAFF(5) ...

        /** Индекс в массиве анимаций CombatAnimationState */
        public final int index;

        WeaponGroup(int index) {
            this.index = index;
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Внутренние структуры
    // ─────────────────────────────────────────────────────────

    /** Прямой маппинг: Item → WeaponGroup (быстрый O(1) поиск) */
    private static final Map<Item, WeaponGroup> ITEM_MAP = new LinkedHashMap<>();

    /** Предикаты для тегов и сложной логики */
    private static final List<PredicateEntry> PREDICATE_LIST = new ArrayList<>();

    private record PredicateEntry(Predicate<ItemStack> predicate, WeaponGroup group) {}

    // ─────────────────────────────────────────────────────────
    //  РЕГИСТРАЦИЯ — заполняй здесь
    // ─────────────────────────────────────────────────────────

    /**
     * Регистрация всех боевых предметов.
     * Вызывается один раз из главного класса мода.
     */
    public static void registerAll() {
        register(WeaponGroup.KUNAI, ModItems.KUNAI.get());

        // ── Катаны ────────────────────────────────────────────────
        // register(WeaponGroup.KATANA, ModItems.KATANA.get());
    }

    // ─────────────────────────────────────────────────────────
    //  API регистрации
    // ─────────────────────────────────────────────────────────

    /** Зарегистрировать конкретный Item в группу */
    public static void register(WeaponGroup group, Item item) {
        ITEM_MAP.put(item, group);
    }

    /** Зарегистрировать предикат (тег, класс, любое условие) в группу */
    public static void registerPredicate(WeaponGroup group, Predicate<ItemStack> predicate) {
        PREDICATE_LIST.add(new PredicateEntry(predicate, group));
    }

    // ─────────────────────────────────────────────────────────
    //  Определение группы для стака в руке
    // ─────────────────────────────────────────────────────────

    /**
     * Определить WeaponGroup для ItemStack.
     *
     * Порядок проверки:
     *   1. Пустой стак → EMPTY
     *   2. Прямое совпадение по Item (O(1))
     *   3. Предикаты по порядку добавления
     *   4. Ничего не подошло → EMPTY
     */
    public static WeaponGroup resolveGroup(ItemStack stack) {
        if (stack.isEmpty()) return WeaponGroup.EMPTY;

        WeaponGroup direct = ITEM_MAP.get(stack.getItem());
        if (direct != null) return direct;

        for (PredicateEntry entry : PREDICATE_LIST) {
            if (entry.predicate().test(stack)) return entry.group();
        }

        return WeaponGroup.EMPTY;
    }

    private CombatWeaponRegistry() {}
}