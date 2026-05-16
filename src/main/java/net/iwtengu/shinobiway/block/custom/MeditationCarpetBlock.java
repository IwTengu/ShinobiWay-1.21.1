package net.iwtengu.shinobiway.block.custom;

import net.iwtengu.shinobiway.animation.AnimationController;
import net.iwtengu.shinobiway.animation.ModAnimations;
import net.iwtengu.shinobiway.combat.CombatHelper;
import net.iwtengu.shinobiway.entity.custom.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MeditationCarpetBlock extends CarpetBlock {

    public MeditationCarpetBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state,
                                            Level level,
                                            BlockPos pos,
                                            Player player,
                                            BlockHitResult hit) {

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (CombatHelper.isActive(player)) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (player.isPassenger()) return InteractionResult.PASS;

        SeatEntity seat = new SeatEntity(
                level,
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5
        );

        level.addFreshEntity(seat);
        player.startRiding(seat, true);

        AnimationController.play(player, ModAnimations.MEDITATION);

        return InteractionResult.CONSUME;
    }
}