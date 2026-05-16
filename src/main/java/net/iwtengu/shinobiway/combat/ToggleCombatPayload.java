package net.iwtengu.shinobiway.combat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                   ToggleCombatPayload                       ║
 * ║  Пакет: Клиент → Сервер                                     ║
 * ║  Отправляется при нажатии клавиши B.                        ║
 * ║  Сервер переключает боевой режим игрока.                    ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public record ToggleCombatPayload() implements CustomPacketPayload {

    public static final Type<ToggleCombatPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            "shinobiway",
                            "toggle_combat"
                    )
            );

    /**
     * Пакет без данных.
     */
    public static final StreamCodec<FriendlyByteBuf, ToggleCombatPayload>
            STREAM_CODEC = StreamCodec.of(

            // encode
            (buf, payload) -> {
                // ничего не записываем
            },

            // decode
            buf -> new ToggleCombatPayload()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ─────────────────────────────────────────────────────────────
    // Обработка пакета
    // ─────────────────────────────────────────────────────────────

    public static void handle(
            ToggleCombatPayload payload,
            IPayloadContext context
    ) {

        context.enqueueWork(() -> {

            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            CombatData data = CombatHelper.getData(player);

            // Переключение режима
            if (data.isActive()) {
                CombatHelper.deactivate(player);
            } else {
                CombatHelper.tryActivate(player);
            }
        });
    }
}