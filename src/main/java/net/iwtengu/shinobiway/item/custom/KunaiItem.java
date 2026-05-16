package net.iwtengu.shinobiway.item.custom;

import net.iwtengu.shinobiway.entity.custom.KunaiEntity;
import net.iwtengu.shinobiway.player.ShurikenMasteryData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class KunaiItem extends Item {

    public KunaiItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack,
                             LivingEntity target,
                             LivingEntity attacker) {

        if (attacker instanceof Player player) {
            target.hurt(
                    attacker.damageSources().playerAttack(player),
                    7.0F
            );
        }

        return true;
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

            float speed = 0.5F + t * 2.0F;   // 0.5 → 2.5
            float spread = 3.0F - t * 2.9F;  // 3.0 → 0.1

            KunaiEntity kunai = new KunaiEntity(level, player);

            // 🔥 FIX: правильная стартовая позиция (ВАЖНО)
            kunai.setPos(
                    player.getX(),
                    player.getEyeY() - 0.1,
                    player.getZ()
            );

            kunai.setItem(stack);

            kunai.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    speed,
                    spread
            );

            level.addFreshEntity(kunai);
        }

        player.getCooldowns().addCooldown(this, 2);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}