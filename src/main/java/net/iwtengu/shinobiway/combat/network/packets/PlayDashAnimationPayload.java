package net.iwtengu.shinobiway.combat.network.packets;

import net.iwtengu.shinobiway.combat.client.animation.CombatAnimationHandler;
import net.iwtengu.shinobiway.combat.client.animation.CombatAnimationTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * ============================================================
 *  PlayDashAnimationPayload — пакет: Сервер → Клиент.
 * ============================================================
 *
 * Сервер подтвердил дэш (проверил кулдаун, условия) и отправляет
 * этот пакет клиенту. Клиент запускает анимацию дэша.
 *
 * Почему отдельный пакет, а не в SyncCombatStatePayload?
 *   SyncCombatStatePayload синхронизирует постоянное состояние
 *   (unlocked/active). Дэш — одноразовое событие, его нельзя
 *   хранить как состояние. Отдельный пакет = чистое разделение.
 *
 * Как подключить:
 *   В CombatNetwork.register() добавь:
 *     registrar.playToClient(
 *         PlayDashAnimationPayload.TYPE,
 *         PlayDashAnimationPayload.STREAM_CODEC,
 *         PlayDashAnimationPayload::handle
 *     );
 *
 *   В DashRequestPayload.handle() после применения импульса добавь:
 *     CombatNetwork.sendToPlayer(new PlayDashAnimationPayload(), player);
 */
public record PlayDashAnimationPayload() implements CustomPacketPayload {

    public static final Type<PlayDashAnimationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("shinobiway", "play_dash_animation"));

    /** Пакет без данных — только сигнал события */
    public static final StreamCodec<FriendlyByteBuf, PlayDashAnimationPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> { /* нет данных */ },
                    buf -> new PlayDashAnimationPayload()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ─────────────────────────────────────────────────────────
    //  Обработчик (выполняется на клиенте)
    // ─────────────────────────────────────────────────────────

    public static void handle(PlayDashAnimationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            // Запускаем анимацию дэша через синглтон хэндлера
            // HANDLER доступен через статический метод тикера
            CombatAnimationTicker.playDash(player);
        });
    }
}