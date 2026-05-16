package net.iwtengu.shinobiway.item.custom;

import net.iwtengu.shinobiway.entity.custom.SenbonEntity;
import net.iwtengu.shinobiway.player.ShurikenMasteryData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SenbonItem extends Item {

    public SenbonItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.PLAYERS,
                0.5F,
                1.0F
        );

        if (!level.isClientSide) {

            int mastery = 0;

            if (player instanceof ServerPlayer serverPlayer) {
                mastery = ShurikenMasteryData.getShurikenMastery(serverPlayer);
            }

            float t = Math.min(mastery, 5000) / 5000.0F;

            float speed = 0.8F + t * 2.5F;
            float spread = 3.0F - t * 2.9F;

            // 🔥 создаём снаряд
            SenbonEntity senbon = new SenbonEntity(level, player);

            senbon.setItem(stack);

            // 💥 КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ СПАВНА
            Vec3 look = player.getLookAngle();
            double x = player.getX() + look.x * 0.5;
            double y = player.getEyeY() - 0.1;
            double z = player.getZ() + look.z * 0.5;

            senbon.setPos(x, y, z);

            senbon.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    speed,
                    spread
            );

            level.addFreshEntity(senbon);
        }

        player.getCooldowns().addCooldown(this, 2);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}