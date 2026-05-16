package net.iwtengu.shinobiway.combat;

import net.iwtengu.shinobiway.combat.CombatAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                  SyncCombatStatePayload                     ║
 * ║  Пакет: Сервер → Клиент                                     ║
 * ║  Синхронизирует unlocked и active состояния.                ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * NeoForge 1.21.1:
 *  - implements CustomPacketPayload (не SimpleChannel!)
 *  - TYPE — обязательный идентификатор пакета
 *  - STREAM_CODEC — читает/пишет данные (заменяет encode/decode)
 *  - handle() — обработчик на принимающей стороне
 */
public record SyncCombatStatePayload(boolean unlocked, boolean active)
        implements CustomPacketPayload {

    /** Уникальный ID пакета — должен совпадать с тем что в регистрации */
    public static final Type<SyncCombatStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("shinobiway", "sync_combat_state"));

    /**
     * StreamCodec — кодирует/декодирует данные пакета.
     * Порядок read/write должен строго совпадать!
     */
    public static final StreamCodec<FriendlyByteBuf, SyncCombatStatePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeBoolean(payload.unlocked());
                        buf.writeBoolean(payload.active());
                    },
                    buf -> new SyncCombatStatePayload(
                            buf.readBoolean(),
                            buf.readBoolean()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ─────────────────────────────────────────────────────────────
    //  Обработчик (вызывается на клиенте при получении пакета)
    // ─────────────────────────────────────────────────────────────

    /**
     * Выполняется на клиенте в главном потоке (mainThread гарантируется
     * при регистрации через .mainThread() в CombatNetwork).
     */
    public static void handle(SyncCombatStatePayload payload, IPayloadContext context) {
        // context.enqueueWork гарантирует выполнение в главном потоке
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            // Обновляем данные клиентского игрока
            var data = player.getData(CombatAttachments.COMBAT_DATA.get());
            data.setUnlocked(payload.unlocked());
            data.setActive(payload.active());
            player.setData(CombatAttachments.COMBAT_DATA.get(), data);

            // ──────────────────────────────────────────────────────
            // МЕСТО ДЛЯ АНИМАЦИЙ (player-animation-lib):
            //
            // if (payload.active()) {
            //     // Вход в боевой режим
            //     KeyframeAnimationPlayer.of(CombatAnimations.ENTER_COMBAT)
            //         .play(player);
            // } else {
            //     // Выход из боевого режима
            //     KeyframeAnimationPlayer.stop(player, CombatAnimations.ENTER_COMBAT);
            // }
            // ──────────────────────────────────────────────────────
        });
    }
}
