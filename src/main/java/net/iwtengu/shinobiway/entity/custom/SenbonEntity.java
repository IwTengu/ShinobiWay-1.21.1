package net.iwtengu.shinobiway.entity.custom;

import net.iwtengu.shinobiway.entity.ModEntities;
import net.iwtengu.shinobiway.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SenbonEntity extends AbstractArrow {

    private ItemStack stack = ItemStack.EMPTY;

    public SenbonEntity(EntityType<? extends SenbonEntity> type, Level level) {
        super(type, level);
    }

    public SenbonEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SENBON.get(), level);

        this.setOwner(shooter);

        this.setBaseDamage(7.0F);
    }

    public void setItem(ItemStack stack) {
        this.stack = stack.copy();
    }

    public ItemStack getSenbonItem() {
        return this.stack.isEmpty()
                ? new ItemStack(ModItems.SENBON.get())
                : this.stack;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.SENBON.get());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isInWater() || this.isInLava()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.75D));
        }
    }

    @Override
    protected void onHitEntity(net.minecraft.world.phys.EntityHitResult result) {
        super.onHitEntity(result);

        if (result.getEntity() instanceof net.minecraft.world.entity.LivingEntity living) {
            int arrows = living.getArrowCount();
            if (arrows > 0) {
                living.setArrowCount(arrows - 1);
            }
        }

        this.discard();
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
    }
}