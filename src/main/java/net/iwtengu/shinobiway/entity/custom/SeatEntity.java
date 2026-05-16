package net.iwtengu.shinobiway.entity.custom;

import net.iwtengu.shinobiway.animation.AnimationController;
import net.iwtengu.shinobiway.animation.ModAnimations;
import net.iwtengu.shinobiway.chakra.ChakraAttachment;
import net.iwtengu.shinobiway.chakra.ChakraData;
import net.iwtengu.shinobiway.chakra.ChakraSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class SeatEntity extends Entity {

    // Таймер для медитации
    // 20 тиков = 1 секунда
    private int meditationTicks = 0;

    // Запоминаем последнего пассажира чтобы остановить анимации при вставании
    private ServerPlayer lastPassenger = null;

    // Флаг: анимация уже запущена для текущего пассажира
    private boolean animationStarted = false;

    public SeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SeatEntity(Level level, double x, double y, double z) {
        this(net.iwtengu.shinobiway.entity.ModEntities.SEAT.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {

        super.tick();

        // Только сервер
        if (level().isClientSide) {
            return;
        }

        // Если никто не сидит — остановить анимации и удалить entity
        if (this.getPassengers().isEmpty()) {

            if (lastPassenger != null) {
                AnimationController.stopAll(lastPassenger);
                lastPassenger = null;
                animationStarted = false;
            }

            this.discard();
            return;
        }

        // Берём первого пассажира
        Entity passenger = this.getPassengers().getFirst();

        // Только игрок
        if (!(passenger instanceof ServerPlayer player)) {
            return;
        }

        // Если пассажир сменился (например после смерти пересел) — сбрасываем флаг
        if (lastPassenger != player) {
            animationStarted = false;
        }

        // Запоминаем текущего пассажира
        lastPassenger = player;

        // Запускаем анимацию только один раз при посадке
        if (!animationStarted) {
            AnimationController.play(player, ModAnimations.MEDITATION);
            animationStarted = true;
        }

        // Увеличиваем таймер
        meditationTicks++;

        // Каждую секунду
        if (meditationTicks >= 20) {

            meditationTicks = 0;

            // Получаем chakra data
            ChakraData chakra = ChakraAttachment.get(player);

            // Увеличиваем максимум чакры на 10
            chakra.addMax(10f);

            // Синхронизируем клиент
            ChakraSyncPacket.send(player);
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }
}