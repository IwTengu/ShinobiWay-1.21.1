package net.iwtengu.shinobiway.chakra;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;

/**
 * Один простой центр всех бонусов регена.
 * КЛАНЫ / ЗЕЛЬЯ / БАФФЫ / ПРЕДМЕТЫ — всё сюда.
 */
public class ChakraRegenSources {

    public static float getBonus(ServerPlayer player) {

        float bonus = 0f;

        // ───────── пример: зелье/бафф ─────────
        if (player.hasEffect(MobEffects.REGENERATION)) {
            bonus += 0.5f;
        }

        if (player.hasEffect(MobEffects.DAMAGE_BOOST)) {
            bonus += 0.5f;
        }



        // ───────── пример: будущие системы ─────────
        // if (player.getCapability(...)) bonus += X;

        return bonus;
    }
}
