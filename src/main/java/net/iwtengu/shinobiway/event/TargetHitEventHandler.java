package net.iwtengu.shinobiway.event;

import net.iwtengu.shinobiway.chakra.ChakraAttachment;
import net.iwtengu.shinobiway.chakra.ChakraData;
import net.iwtengu.shinobiway.chakra.ChakraSyncPacket;
import net.iwtengu.shinobiway.combat.CombatHelper;
import net.iwtengu.shinobiway.entity.custom.KunaiEntity;
import net.iwtengu.shinobiway.entity.custom.ShurikenEntity;
import net.iwtengu.shinobiway.player.ShurikenMasteryData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

public class TargetHitEventHandler {

    public static void register() {
        NeoForge.EVENT_BUS.addListener(TargetHitEventHandler::onProjectileImpact);
    }

    private static void onProjectileImpact(ProjectileImpactEvent event) {

        Projectile projectile = event.getProjectile();

        if (!(projectile instanceof ShurikenEntity) &&
                !(projectile instanceof KunaiEntity)) {
            return;
        }

        if (event.getRayTraceResult().getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult hitResult = (BlockHitResult) event.getRayTraceResult();

        Level level = projectile.level();
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof TargetBlock)) {
            return;
        }

        int signal = getRedstoneStrength(hitResult);

        if (signal != 15) {
            return;
        }

        Entity owner = projectile.getOwner();

        if (!(owner instanceof ServerPlayer player)) {
            return;
        }

        Vec3 targetCenter = Vec3.atCenterOf(pos);
        double distance = player.position().distanceTo(targetCenter);

        if (distance < 3.0D) {
            return;
        }

        ShurikenMasteryData.addShurikenMastery(player, 1);
        giveAdvancement(player);
    }

    private static void giveAdvancement(ServerPlayer player) {

        ResourceLocation id = ResourceLocation.tryParse("shinobiway:myshinobiway");
        if (id == null) return;

        AdvancementHolder advancement = player.server
                .getAdvancements()
                .get(id);

        if (advancement == null) return;

        PlayerAdvancements progress = player.getAdvancements();

        if (!progress.getOrStartProgress(advancement).isDone()) {
            progress.award(advancement, "impossible");

            ChakraData chakra = ChakraAttachment.get(player);
            chakra.unlock();
            ChakraSyncPacket.send((ServerPlayer) player);

            CombatHelper.unlock((ServerPlayer) player);
        }
    }

    private static int getRedstoneStrength(BlockHitResult hitResult) {

        Vec3 hit = hitResult.getLocation();

        double dx = Math.abs(hit.x - hitResult.getBlockPos().getX() - 0.5D);
        double dy = Math.abs(hit.y - hitResult.getBlockPos().getY() - 0.5D);
        double dz = Math.abs(hit.z - hitResult.getBlockPos().getZ() - 0.5D);

        double max;

        switch (hitResult.getDirection().getAxis()) {
            case Y -> max = Math.max(dx, dz);
            case Z -> max = Math.max(dx, dy);
            default -> max = Math.max(dy, dz);
        }

        return Math.max(1, 15 - (int)(max * 15.0D));
    }
}