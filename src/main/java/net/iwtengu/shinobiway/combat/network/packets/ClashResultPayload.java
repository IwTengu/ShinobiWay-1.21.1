package net.iwtengu.shinobiway.combat.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                   ClashResultPayload                        ║
 * ║  Пакет: Сервер → Клиент                                     ║
 * ║  Сообщает клиенту о столкновении — воспроизвести звук/эффект║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * ── Подключить в CombatNetwork.register(): ────────────────────
 *   registrar.playToClient(
 *       ClashResultPayload.TYPE,
 *       ClashResultPayload.STREAM_CODEC,
 *       ClashResultPayload::handle
 *   );
 */
public record ClashResultPayload(Effect effect) implements CustomPacketPayload {

    /**
     * Тип эффекта столкновения.
     * Передаётся как byte по сети.
     */
    public enum Effect {
        WEAPONS_CLASH,      // звон клинков
        FIST_CLASH,         // глухой удар кулак в кулак
        UNARMED_VS_ARMED,   // удар кулаком о клинок
        PERFECT_DISARM;     // идеальный перехват

        /** Все значения для десериализации по индексу */
        private static final Effect[] VALUES = values();

        public static Effect byIndex(int i) {
            return i >= 0 && i < VALUES.length ? VALUES[i] : WEAPONS_CLASH;
        }
    }

    public static final Type<ClashResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("shinobiway", "clash_result"));

    public static final StreamCodec<FriendlyByteBuf, ClashResultPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> buf.writeByte(p.effect().ordinal()),
                    buf -> new ClashResultPayload(Effect.byIndex(buf.readByte()))
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ─────────────────────────────────────────────────────────
    //  Обработчик (клиент)
    // ─────────────────────────────────────────────────────────

    public static void handle(ClashResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            switch (payload.effect()) {

                case WEAPONS_CLASH -> {
                    // Звон клинков
                    player.level().playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.SHIELD_BLOCK,
                            SoundSource.PLAYERS, 1.0f, 1.2f, false
                    );

                    // ── МЕСТО ДЛЯ ВИЗУАЛЬНОГО ЭФФЕКТА (искры) ─────
                    // Minecraft.getInstance().level.addParticle(
                    //     ParticleTypes.CRIT, player.getX(), player.getY()+1, player.getZ(),
                    //     0, 0, 0);
                    // ──────────────────────────────────────────────
                }

                case FIST_CLASH -> {
                    // Глухой удар кулак в кулак
                    player.level().playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_HURT,
                            SoundSource.PLAYERS, 0.8f, 0.7f, false
                    );
                }

                case UNARMED_VS_ARMED -> {
                    // Удар кулаком о клинок — болезненный звук
                    player.level().playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_HURT,
                            SoundSource.PLAYERS, 1.0f, 0.5f, false
                    );

                    // ── МЕСТО ДЛЯ ДОПОЛНИТЕЛЬНЫХ ЭФФЕКТОВ ─────────
                    // (например экранный flash, particles)
                    // ──────────────────────────────────────────────
                }

                case PERFECT_DISARM -> {
                    // Идеальный перехват — эффектный звук
                    player.level().playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS, 1.0f, 2.0f, false
                    );
                    player.level().playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.SHIELD_BLOCK,
                            SoundSource.PLAYERS, 0.8f, 0.5f, false
                    );

                    // ── МЕСТО ДЛЯ АНИМАЦИИ ПЕРЕХВАТА ──────────────
                    // AnimationController.play(player, ModAnimations.DISARM);
                    // ──────────────────────────────────────────────
                }
            }
        });
    }
}