package net.iwtengu.shinobiway.entity.custom;

import net.iwtengu.shinobiway.entity.ModEntities;
import net.iwtengu.shinobiway.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShurikenEntity extends AbstractArrow {

    private ItemStack stack = ItemStack.EMPTY;

    // =========================
    // 📌 ОСНОВНОЙ КОНСТРУКТОР
    // =========================
    public ShurikenEntity(EntityType<? extends ShurikenEntity> type, Level level) {
        super(type, level);
    }

    // =========================
    // 🌀 КОНСТРУКТОР БРОСКА
    // =========================
    public ShurikenEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SHURIKEN.get(), level);

        this.setOwner(shooter);

        this.setPos(
                shooter.getX(),
                shooter.getEyeY() - 0.1F,
                shooter.getZ()
        );

        this.setBaseDamage(4.0D);
    }

    // =========================
    // 📦 ITEM ДЛЯ РЕНДЕРА
    // =========================
    public void setItem(ItemStack stack) {
        this.stack = stack.copy();
    }

    public ItemStack getShurikenItem() {
        return this.stack.isEmpty()
                ? new ItemStack(ModItems.SHURIKEN.get())
                : this.stack;
    }

    public boolean isInGround() {
        return this.inGround;
    }

    // =========================
    // 🎯 ПОДБОР
    // =========================
    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.SHURIKEN.get());
    }

    // =========================
    // 🧠 TICK
    // =========================
    @Override
    public void tick() {
        super.tick();

        // замедление в воде/лаве
        if (this.isInWater() || this.isInLava()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.75D));
        }

        // поворот по направлению полёта
        if (!this.inGround) {

            var vec = this.getDeltaMovement();

            double horizontal = Math.sqrt(vec.x * vec.x + vec.z * vec.z);

            this.setYRot((float)(Math.atan2(vec.x, vec.z) * (180F / Math.PI)));
            this.setXRot((float)(Math.atan2(vec.y, horizontal) * (180F / Math.PI)));
        }
    }

    // =========================
    // 💥 ПОПАДАНИЕ В ENTITY
    // =========================
    @Override
    protected void onHitEntity(net.minecraft.world.phys.EntityHitResult result) {
        super.onHitEntity(result);

        // убираем ванильную стрелу из тела
        if (result.getEntity() instanceof net.minecraft.world.entity.LivingEntity living) {

            int arrows = living.getArrowCount();

            if (arrows > 0) {
                living.setArrowCount(arrows - 1);
            }
        }

        this.discard();
    }
}