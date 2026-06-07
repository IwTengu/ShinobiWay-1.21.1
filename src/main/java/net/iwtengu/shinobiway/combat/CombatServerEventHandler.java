package net.iwtengu.shinobiway.combat;

import net.iwtengu.shinobiway.combat.debug.ClashTestBuffer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * ============================================================
 *  CombatServerEventHandler
 * ============================================================
 */
@EventBusSubscriber(modid = "shinobiway")
public class CombatServerEventHandler {

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CombatData data = CombatHelper.getData(player);
        data.tickDashCooldown();
        data.tickAttackCooldown();
        data.tickComboReset();
        data.tickClashWindow();

        CombatHelper.tickCooldowns(player);

        // [DEBUG] Тикаем тестовый буфер — удали перед релизом
        ClashTestBuffer.tickAll();
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CombatData data = CombatHelper.getData(player);
        if (data.isActive()) {
            data.setActive(false);
            data.resetCombo();
            data.closeClashWindow();
            player.setData(CombatAttachments.COMBAT_DATA.get(), data);
            CombatHelper.syncToClient(player);
        }
        // [DEBUG] — удали перед релизом
        ClashTestBuffer.clear(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CombatData data = CombatHelper.getData(player);
        if (data.isActive()) {
            data.setActive(false);
            player.setData(CombatAttachments.COMBAT_DATA.get(), data);
        }
        CombatHelper.syncToClient(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CombatHelper.syncToClient(player);
    }
}