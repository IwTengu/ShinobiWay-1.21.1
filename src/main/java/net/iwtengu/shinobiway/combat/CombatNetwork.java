package net.iwtengu.shinobiway.combat;

import net.iwtengu.shinobiway.combat.network.packets.AttackRequestPayload;
import net.iwtengu.shinobiway.combat.network.packets.ClashResultPayload;
import net.iwtengu.shinobiway.combat.network.packets.PlayAttackAnimationPayload;
import net.iwtengu.shinobiway.combat.network.packets.PlayDashAnimationPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * ============================================================
 *  CombatNetwork — регистрация всех пакетов боевой системы.
 * ============================================================
 */
public class CombatNetwork {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("shinobiway").versioned("1");

        // Клиент → Сервер: переключение боевого режима
        registrar.playToServer(
                ToggleCombatPayload.TYPE,
                ToggleCombatPayload.STREAM_CODEC,
                ToggleCombatPayload::handle
        );

        // Клиент → Сервер: запрос дэша
        registrar.playToServer(
                DashRequestPayload.TYPE,
                DashRequestPayload.STREAM_CODEC,
                DashRequestPayload::handle
        );

        // Клиент → Сервер: запрос атаки (ЛКМ в боевом режиме)
        registrar.playToServer(
                AttackRequestPayload.TYPE,
                AttackRequestPayload.STREAM_CODEC,
                AttackRequestPayload::handle
        );

        // Сервер → Клиент: синхронизация состояния боевого режима
        registrar.playToClient(
                SyncCombatStatePayload.TYPE,
                SyncCombatStatePayload.STREAM_CODEC,
                SyncCombatStatePayload::handle
        );

        // Сервер → Клиент: запустить анимацию дэша
        registrar.playToClient(
                PlayDashAnimationPayload.TYPE,
                PlayDashAnimationPayload.STREAM_CODEC,
                PlayDashAnimationPayload::handle
        );

        // Сервер → Клиент: запустить анимацию удара
        registrar.playToClient(
                PlayAttackAnimationPayload.TYPE,
                PlayAttackAnimationPayload.STREAM_CODEC,
                PlayAttackAnimationPayload::handle
        );

        // Сервер → Клиент: результат столкновения (звук + эффект)
        registrar.playToClient(
                ClashResultPayload.TYPE,
                ClashResultPayload.STREAM_CODEC,
                ClashResultPayload::handle
        );
    }

    public static void sendToPlayer(
            net.minecraft.network.protocol.common.custom.CustomPacketPayload payload,
            ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToServer(
            net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}