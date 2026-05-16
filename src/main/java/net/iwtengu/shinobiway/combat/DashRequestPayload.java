package net.iwtengu.shinobiway.combat;

import net.iwtengu.shinobiway.animation.AnimationController;
import net.iwtengu.shinobiway.animation.ModAnimations;
import net.iwtengu.shinobiway.combat.CombatAttachments;
import net.iwtengu.shinobiway.combat.CombatData;
import net.iwtengu.shinobiway.combat.CombatHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                    DashRequestPayload                       ║
 * ║  Пакет: Клиент → Сервер                                     ║
 * ║  Клиент отправляет направление дэша, сервер применяет импульс.║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Почему именно так:
 *  Клиент вычисляет направление (он знает куда смотрит игрок),
 *  но движение применяется ТОЛЬКО на сервере — иначе античит откатит.
 */
public record DashRequestPayload(double dirX, double dirZ) implements CustomPacketPayload {

    public static final Type<DashRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("shinobiway", "dash_request"));

    public static final StreamCodec<FriendlyByteBuf, DashRequestPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeDouble(payload.dirX());
                        buf.writeDouble(payload.dirZ());
                    },
                    buf -> new DashRequestPayload(buf.readDouble(), buf.readDouble())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ─────────────────────────────────────────────────────────────
    //  Обработчик (выполняется на сервере)
    // ─────────────────────────────────────────────────────────────

    public static void handle(DashRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            CombatData data = CombatHelper.getData(player);

            // ── Проверки безопасности ──────────────────────────────
            if (!data.isActive())     return; // режим не активен
            if (!data.canDash())      return; // кулдаун ещё не прошёл
            if (player.isPassenger()) return; // на транспорте нельзя

            // ── Нормализуем вектор (не доверяем длине от клиента) ──
            Vec3 dir = new Vec3(payload.dirX(), 0, payload.dirZ()).normalize();

            // ── Применяем импульс ──────────────────────────────────
            // 0.9 — горизонтальная сила (~2 блока при нормальной физике)
            // 0.3 — лёгкий вертикальный подброс для ощущения дэша
            player.setDeltaMovement(dir.x * 0.9, 0.3, dir.z * 0.9);

            // Уведомляем клиент что нужно обновить позицию
            player.hurtMarked = true;

            // ── Ставим кулдаун ─────────────────────────────────────
            data.setDashCooldown(CombatData.DASH_MAX_COOLDOWN);
            player.setData(CombatAttachments.COMBAT_DATA.get(), data);

            // ──────────────────────────────────────────────────────
            // МЕСТО ДЛЯ ДОПОЛНИТЕЛЬНЫХ ЭФФЕКТОВ ДЭША (сервер):
            //
            // Звук:
               player.level().playSound(null, player.blockPosition(),
                   SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.5f);
            //
            // Эффект неуязвимости на время дэша (5 тиков):
            //   player.addEffect(new MobEffectInstance(
            //       MobEffects.DAMAGE_RESISTANCE, 5, 4, false, false));
            //
            // Отправить пакет клиенту для анимации дэша:
            // CombatNetwork.sendToPlayer(new PlayDashAnimationPayload(), player);
            // ──────────────────────────────────────────────────────

            ItemStack hand = player.getMainHandItem();
            boolean empty = hand.isEmpty();
            if (empty) {
                AnimationController.stopAll(player);
                AnimationController.play(player, ModAnimations.EMPTY_RUN);
            } else {
                AnimationController.stopAll(player);
                AnimationController.play(player, ModAnimations.EMPTY_SHIFT);
            }
            // Синхронизируем обновлённые данные с клиентом
            CombatHelper.syncToClient(player);
        });
    }
}