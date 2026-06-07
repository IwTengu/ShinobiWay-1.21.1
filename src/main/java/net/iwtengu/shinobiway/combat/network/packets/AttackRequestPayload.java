package net.iwtengu.shinobiway.combat.network.packets;

import net.iwtengu.shinobiway.combat.*;
import net.iwtengu.shinobiway.combat.clash.ClashDetector;
import net.iwtengu.shinobiway.combat.client.animation.CombatWeaponRegistry;
import net.iwtengu.shinobiway.combat.client.animation.CombatWeaponRegistry.WeaponGroup;
import net.iwtengu.shinobiway.combat.debug.ClashTestBuffer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record AttackRequestPayload() implements CustomPacketPayload {

    public static final Type<AttackRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("shinobiway", "attack_request"));

    public static final StreamCodec<FriendlyByteBuf, AttackRequestPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {},
                    buf -> new AttackRequestPayload()
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AttackRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            CombatData data = CombatHelper.getData(player);
            if (!data.isActive()) return;
            if (!data.canAttack()) return;

            WeaponGroup group = CombatWeaponRegistry.resolveGroup(player.getMainHandItem());

            int hitIndex = data.advanceCombo();
            int cooldown = CombatAttackRegistry.getCooldown(group);
            data.setAttackCooldown(cooldown);

            // Открываем окно столкновения
            int clashWindow = CombatAttackRegistry.getClashWindow(group);
            data.openClashWindow(clashWindow, group.index);
            player.setData(CombatAttachments.COMBAT_DATA.get(), data);

            // ── [DEBUG] Проверка тестового буфера ─────────────────
            // Если у самого игрока есть тестовое окно — симулируем
            // клэш с самим собой (для тестирования в одиночной игре).
            // УДАЛИ этот блок перед релизом вместе с ClashTestBuffer.
            if (ClashTestBuffer.has(player)) {
                ClashTestBuffer.Entry testEntry = ClashTestBuffer.get(player);
                if (testEntry != null) {
                    ClashTestBuffer.clear(player);
                    // Симулируем CombatData "второго игрока" из буфера
                    ClashDetector.tryClashWithBuffer(player, group, testEntry);
                    // Анимация удара всё равно играет
                    CombatNetwork.sendToPlayer(
                            new PlayAttackAnimationPayload(hitIndex, group.index), player);
                    return; // выходим — реальный хитбокс не нужен
                }
            }
            // ── [/DEBUG] ──────────────────────────────────────────

            // Хитбокс
            Vec3 look = player.getLookAngle();

            double range = switch (group) {
                case EMPTY -> 2.2;
                case KUNAI -> 2.0;
                case KATANA -> 2.5;
            };

            double width = switch (group) {
                case EMPTY -> 0.25;
                case KUNAI -> 0.6;
                case KATANA -> 0.85;
            };

            Vec3 center = player.position().add(0, 1.2, 0).add(look.scale(range));
            Vec3 right   = new Vec3(-look.z, 0, look.x).normalize();
            Vec3 halfRight = right.scale(width);
            Vec3 forward   = look.scale(0.6);
            Vec3 min = center.subtract(halfRight).subtract(new Vec3(0, 0.8, 0)).subtract(forward);
            Vec3 max = center.add(halfRight).add(new Vec3(0, 0.8, 0)).add(forward);
            AABB hitBox = new AABB(min, max);

            float damage = switch (group) {
                case EMPTY -> 4.0f;
                case KUNAI -> 5.5f;
                case KATANA -> 7.0f;
            };

            List<Entity> targets = player.level().getEntities(player, hitBox);
            for (Entity e : targets) {
                if (!(e instanceof LivingEntity living)) continue;
                if (e == player) continue;

                Vec3 toTarget = e.position().subtract(player.position()).normalize();
                if (toTarget.dot(look) < 0.15) continue;

                if (e instanceof ServerPlayer targetPlayer) {
                    boolean clashed = ClashDetector.tryClash(player, targetPlayer, group);
                    if (clashed) continue;
                }

                living.hurt(player.damageSources().playerAttack(player), damage);
            }

            CombatNetwork.sendToPlayer(
                    new PlayAttackAnimationPayload(hitIndex, group.index), player);
        });
    }
}