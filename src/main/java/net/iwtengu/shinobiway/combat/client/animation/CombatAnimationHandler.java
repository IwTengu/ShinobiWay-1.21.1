package net.iwtengu.shinobiway.combat.client.animation;

import net.iwtengu.shinobiway.animation.AnimationController;
import net.iwtengu.shinobiway.animation.AnimationDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class CombatAnimationHandler {

    private CombatAnimationState lastState = null;
    private CombatWeaponRegistry.WeaponGroup lastGroup = null;

    // =========================================================
    // TICK
    // =========================================================

    public void tick(LocalPlayer player) {

        CombatAnimationState currentState = resolveState(player);

        ItemStack held = player.getMainHandItem();
        CombatWeaponRegistry.WeaponGroup currentGroup =
                CombatWeaponRegistry.resolveGroup(held);

        if (currentState == lastState && currentGroup == lastGroup) {
            return;
        }

        if (lastState != null && lastGroup != null) {
            AnimationDefinition oldAnim =
                    lastState.getAnimation(lastGroup);

            AnimationController.stop(player, oldAnim);
        }

        AnimationDefinition newAnim =
                currentState.getAnimation(currentGroup);

        AnimationController.play(player, newAnim);

        lastState = currentState;
        lastGroup = currentGroup;
    }

    // =========================================================
    // RESET
    // =========================================================

    public void reset(LocalPlayer player) {

        if (lastState != null && lastGroup != null) {
            AnimationController.stop(
                    player,
                    lastState.getAnimation(lastGroup)
            );
        }

        lastState = null;
        lastGroup = null;
    }

    public void forceRefresh() {
        lastState = null;
        lastGroup = null;
    }

    // =========================================================
    // DASH
    // =========================================================

    public void playDash(LocalPlayer player) {

        if (lastState != null && lastGroup != null) {
            AnimationController.stop(
                    player,
                    lastState.getAnimation(lastGroup)
            );
        }

        CombatWeaponRegistry.WeaponGroup currentGroup =
                CombatWeaponRegistry.resolveGroup(
                        player.getMainHandItem()
                );

        AnimationController.play(
                player,
                CombatAnimationState.DASH.getAnimation(currentGroup)
        );

        lastState = null;
        lastGroup = null;
    }

    // =========================================================
    // ATTACK  <<< ВОТ ЭТОГО У ТЕБЯ НЕ ХВАТАЛО
    // =========================================================

    public void playAttack(
            LocalPlayer player,
            int hitIndex,
            int groupIndex
    ) {

        if (lastState != null && lastGroup != null) {
            AnimationController.stop(
                    player,
                    lastState.getAnimation(lastGroup)
            );
        }

        CombatAnimationState attackState =
                CombatAnimationState.attackByIndex(hitIndex);

        AnimationDefinition attackAnimation =
                attackState.getAnimationByGroupIndex(groupIndex);

        AnimationController.play(
                player,
                attackAnimation
        );

        lastState = null;
        lastGroup = null;
    }

    // =========================================================
    // STATE RESOLVE
    // =========================================================

    private CombatAnimationState resolveState(LocalPlayer player) {

        if (!player.onGround()
                && !player.isInWater()
                && !player.isSwimming()) {

            return CombatAnimationState.JUMP;
        }

        if (player.isSprinting()
                && !player.isCrouching()) {

            return CombatAnimationState.RUN;
        }

        if (player.isCrouching()) {
            return CombatAnimationState.SNEAK;
        }

        double speedSq = horizontalSpeedSq(player);

        if (speedSq > 0.001 * 0.001) {

            return isMovingBackward(player)
                    ? CombatAnimationState.WALK_BACK
                    : CombatAnimationState.WALK;
        }

        return CombatAnimationState.IDLE;
    }

    // =========================================================
    // MOVEMENT CHECK
    // =========================================================

    private boolean isMovingBackward(LocalPlayer player) {

        Vec3 velocity = player.getDeltaMovement();

        Vec3 moveDir =
                new Vec3(
                        velocity.x,
                        0,
                        velocity.z
                ).normalize();

        float yaw = player.getYRot();

        double fwdX =
                -Math.sin(Math.toRadians(yaw));

        double fwdZ =
                Math.cos(Math.toRadians(yaw));

        double dot =
                moveDir.x * fwdX
                        + moveDir.z * fwdZ;

        return dot < -0.3;
    }

    private double horizontalSpeedSq(LocalPlayer player) {

        double dx =
                player.getDeltaMovement().x;

        double dz =
                player.getDeltaMovement().z;

        return dx * dx + dz * dz;
    }
}