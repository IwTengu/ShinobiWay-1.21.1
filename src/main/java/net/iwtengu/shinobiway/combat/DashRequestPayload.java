package net.iwtengu.shinobiway.combat;

import net.iwtengu.shinobiway.combat.CombatAttachments;
import net.iwtengu.shinobiway.combat.CombatData;
import net.iwtengu.shinobiway.combat.CombatHelper;
import net.iwtengu.shinobiway.combat.network.packets.PlayDashAnimationPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                    DashRequestPayload                       ║
 * ║  Пакет: Клиент → Сервер                                     ║
 * ║  Клиент отправляет направление дэша, сервер применяет импульс.║
 * ╚══════════════════════════════════════════════════════════════╝
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

            // ── Запрет дэша в воде и лаве ─────────────────────────
            // isInWater()   — игрок в воде (включая плавание)
            // isInLava()    — игрок в лаве
            // isSwimming()  — игрок в режиме плавания (Ctrl+пробел в воде)
            // Все три случая запрещаем — дэш в жидкости не имеет смысла
            // и может использоваться как эксплойт для быстрого плавания
            if (player.isInWater() || player.isInLava() || player.isSwimming()) return;

            // ── Запрет дэша на элитрах ────────────────────────────
            // isFallFlying() — true когда игрок летит на элитрах.
            // Это именно полёт на элитрах, не обычное падение:
            // Minecraft выставляет этот флаг только при активном планировании.
            if (player.isFallFlying()) return;

            // ── Нормализуем вектор (не доверяем длине от клиента) ──
            Vec3 dir = new Vec3(payload.dirX(), 0, payload.dirZ()).normalize();

            // ── Применяем импульс ──────────────────────────────────
            player.setDeltaMovement(dir.x * 0.9, 0.3, dir.z * 0.9);
            player.hurtMarked = true;

            // ── Ставим кулдаун ─────────────────────────────────────
            data.setDashCooldown(CombatData.DASH_MAX_COOLDOWN);
            player.setData(CombatAttachments.COMBAT_DATA.get(), data);

            // ── Звук дэша ──────────────────────────────────────────
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.5f);

            // ── Анимация дэша на клиенте ───────────────────────────
            CombatNetwork.sendToPlayer(new PlayDashAnimationPayload(), player);

            // ── Синхронизируем кулдаун с клиентом ─────────────────
            CombatHelper.syncToClient(player);
        });
    }
}