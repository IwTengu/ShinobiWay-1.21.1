package net.iwtengu.shinobiway.combat.network.packets;

import net.iwtengu.shinobiway.combat.client.animation.CombatAnimationTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                PlayAttackAnimationPayload                   ║
 * ║  Пакет: Сервер → Клиент                                     ║
 * ║  Сервер подтвердил удар — клиент запускает анимацию.        ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Несёт два числа:
 *   hitIndex   — номер удара в комбо (0, 1, 2)
 *   groupIndex — индекс группы оружия (WeaponGroup.index)
 *
 * По ним CombatAnimationTicker.playAttack() выбирает
 * нужную анимацию из CombatAnimationState.
 *
 * ── Как подключить ────────────────────────────────────────────
 *  В CombatNetwork.register():
 *    registrar.playToClient(
 *        PlayAttackAnimationPayload.TYPE,
 *        PlayAttackAnimationPayload.STREAM_CODEC,
 *        PlayAttackAnimationPayload::handle
 *    );
 */
public record PlayAttackAnimationPayload(int hitIndex, int groupIndex)
        implements CustomPacketPayload {

    public static final Type<PlayAttackAnimationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("shinobiway", "play_attack_animation"));

    public static final StreamCodec<FriendlyByteBuf, PlayAttackAnimationPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeByte(p.hitIndex());   // 0/1/2 — влезает в byte
                        buf.writeByte(p.groupIndex()); // индекс группы
                    },
                    buf -> new PlayAttackAnimationPayload(buf.readByte(), buf.readByte())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ─────────────────────────────────────────────────────────
    //  Обработчик (выполняется на клиенте)
    // ─────────────────────────────────────────────────────────

    public static void handle(PlayAttackAnimationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            // Передаём в тикер — он остановит текущую loco-анимацию
            // и запустит нужную анимацию удара
            CombatAnimationTicker.playAttack(player, payload.hitIndex(), payload.groupIndex());
        });
    }
}