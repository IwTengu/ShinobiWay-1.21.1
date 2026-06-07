package net.iwtengu.shinobiway.util;

import net.iwtengu.shinobiway.component.EyeOwnerComponent;
import net.iwtengu.shinobiway.component.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EyeOwnerHelper {

    /**
     * Привязывает глаз к игроку если он ещё не привязан.
     * Вызывай при надевании глаза в слот.
     */
    public static void bindIfNeeded(ItemStack stack, ServerPlayer player) {
        if (!isBound(stack)) {
            stack.set(ModDataComponents.EYE_OWNER.get(),
                    new EyeOwnerComponent(player.getUUID(), player.getName().getString())
            );
        }
    }

    /**
     * Проверяет привязан ли глаз к конкретному игроку.
     * Используй везде где нужна проверка "родной ли глаз".
     */
    public static boolean isOwnedBy(ItemStack stack, ServerPlayer player) {
        EyeOwnerComponent component = stack.get(ModDataComponents.EYE_OWNER.get());
        if (component == null) return false;
        return component.ownerUUID().equals(player.getUUID());
    }

    /**
     * Проверяет привязан ли глаз вообще к кому-либо.
     */
    public static boolean isBound(ItemStack stack) {
        return stack.has(ModDataComponents.EYE_OWNER.get());
    }

    /**
     * Возвращает ник владельца или null если глаз не привязан.
     */
    public static String getOwnerName(ItemStack stack) {
        EyeOwnerComponent component = stack.get(ModDataComponents.EYE_OWNER.get());
        return component != null ? component.ownerName() : null;
    }
}