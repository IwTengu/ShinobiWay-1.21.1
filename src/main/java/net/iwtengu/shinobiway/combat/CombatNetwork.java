package net.iwtengu.shinobiway.combat;

import net.iwtengu.shinobiway.combat.DashRequestPayload;
import net.iwtengu.shinobiway.combat.SyncCombatStatePayload;
import net.iwtengu.shinobiway.combat.ToggleCombatPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                      CombatNetwork                          ║
 * ║  Регистрирует все пакеты боевой системы.                    ║
 * ║  NeoForge 1.21.1: RegisterPayloadHandlersEvent на mod bus.  ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Добавление нового пакета:
 *  1. Создай record implements CustomPacketPayload в packets/
 *  2. Определи TYPE, STREAM_CODEC, handle()
 *  3. Зарегистрируй здесь через registrar.playToServer / playToClient
 */
public class CombatNetwork {

    /**
     * Регистрация всех пакетов.
     * Подписывается на RegisterPayloadHandlersEvent в ShinobiWayMod (mod bus).
     */
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Получаем регистратор для нашего мода с версией протокола
        final PayloadRegistrar registrar = event.registrar("shinobiway").versioned("1");

        // Клиент → Сервер: переключение боевого режима (кнопка B)
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

        // Сервер → Клиент: синхронизация состояния боевого режима
        registrar.playToClient(
                SyncCombatStatePayload.TYPE,
                SyncCombatStatePayload.STREAM_CODEC,
                SyncCombatStatePayload::handle
        );

        // ── Место для новых пакетов ────────────────────────────
        // registrar.playToServer(MyPayload.TYPE, MyPayload.STREAM_CODEC, MyPayload::handle);
    }

    // ─────────────────────────────────────────────────────────────
    //  Утилиты отправки
    // ─────────────────────────────────────────────────────────────

    /**
     * Отправить пакет конкретному игроку (Сервер → Клиент).
     */
    public static void sendToPlayer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload,
                                    ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    /**
     * Отправить пакет с клиента на сервер.
     * Вызывается ТОЛЬКО на клиенте!
     */
    public static void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}