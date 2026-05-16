package net.iwtengu.shinobiway.animation;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public final class PlayerAnimationManager {

    private static final List<AnimationDefinition> DEFINITIONS = new ArrayList<>();

    public static void register(AnimationDefinition def) {
        DEFINITIONS.add(def);
    }

    public static void registerAll() {
        for (java.lang.reflect.Field field : ModAnimations.class.getDeclaredFields()) {
            if (field.getType() == AnimationDefinition.class) {
                try {
                    field.setAccessible(true);
                    AnimationDefinition def = (AnimationDefinition) field.get(null);
                    if (def != null) DEFINITIONS.add(def);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<AnimationDefinition> getAllDefinitions() {
        return Collections.unmodifiableList(DEFINITIONS);
    }

    private static final Map<UUID, PlayerAnimationState> STATES = new ConcurrentHashMap<>();

    public static PlayerAnimationState getOrCreateState(AbstractClientPlayer player) {
        return STATES.computeIfAbsent(player.getUUID(),
                uuid -> PlayerAnimationState.create(player));
    }

    public static PlayerAnimationState get(UUID uuid) {
        return STATES.get(uuid);
    }

    public static PlayerAnimationState get(Player player) {
        if (!(player instanceof AbstractClientPlayer clientPlayer)) return null;
        return getOrCreateState(clientPlayer);
    }

    // Локальный клиент отключается — очищаем всё
    @SubscribeEvent
    public static void onLocalPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        STATES.clear();
    }

    // Другой игрок покидает сервер
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }

    // После смерти — сбрасываем состояние чтобы слои пересоздались для нового объекта игрока
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }

    private PlayerAnimationManager() {}
}