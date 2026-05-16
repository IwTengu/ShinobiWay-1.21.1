package net.iwtengu.shinobiway.item.custom;

import net.iwtengu.shinobiway.entity.custom.ShurikenEntity;
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

public class ShurikenItem extends Item {

    public ShurikenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        // 🔊 звук (можно и клиент и сервер)
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

        // 💥 сервер: только логика броска
        if (!level.isClientSide()) {

            int mastery = 0;

            if (player instanceof ServerPlayer sp) {
                mastery = ShurikenMasteryData.getShurikenMastery(sp);
            }

            float t = Math.min(mastery, 5000) / 5000.0F;

            float speed = 0.5F + t * 2.0F;
            float spread = 3.0F - t * 2.9F;

            ShurikenEntity shuriken = new ShurikenEntity(level, player);

            shuriken.setItem(stack.copy());

            shuriken.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    speed,
                    spread
            );

            level.addFreshEntity(shuriken);
        }

        // ⏱ cooldown
        player.getCooldowns().addCooldown(this, 2);

        // 🧾 расход
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}