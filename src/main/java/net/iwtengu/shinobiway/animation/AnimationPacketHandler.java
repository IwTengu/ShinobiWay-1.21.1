package net.iwtengu.shinobiway.animation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

/**
 * ============================================================
 *  AnimationPacketHandler — сетевой слой системы анимаций.
 * ============================================================
 *
 * Содержит три типа пакетов:
 *
 *  1. C→S  {@link PlayAnimC2SPayload}    — клиент сообщает серверу
 *           "я начал играть анимацию X"
 *
 *  2. S→C  {@link PlayAnimS2CPayload}    — сервер говорит другим клиентам
 *           "у игрока UUID играет анимация X"
 *
 *  3. C→S  {@link StopAnimC2SPayload}    — клиент: "я остановил анимацию X"
 *     S→C  {@link StopAnimS2CPayload}    — сервер: "у игрока UUID стоп анимации X"
 *
 *  4. C→S  {@link StopAllAnimC2SPayload} — клиент: "стоп всех анимаций"
 *     S→C  {@link StopAllAnimS2CPayload} — сервер: "у игрока UUID стоп всех"
 *
 * Регистрация пакетов:
 *   Подпишись на {@link RegisterPayloadHandlersEvent} в главном классе мода:
 * <pre>
 *   modBus.addListener(AnimationPacketHandler::registerPackets);
 * </pre>
 *
 * Поток данных:
 *   Игрок нажимает кнопку
 *     → AnimationController.play() на клиенте
 *       → playLocal() [мгновенно для самого игрока]
 *       → sendPlayToServer() [C→S пакет]
 *         → сервер получает, проверяет, broadcastPlay() → другие клиенты
 *           → каждый клиент вызывает playLocal() для того игрока
 */
public final class AnimationPacketHandler {

    // ID пакетов (должны быть уникальны в моде)
    private static final ResourceLocation PLAY_C2S_ID  = rl("play_anim_c2s");
    private static final ResourceLocation PLAY_S2C_ID  = rl("play_anim_s2c");
    private static final ResourceLocation STOP_C2S_ID  = rl("stop_anim_c2s");
    private static final ResourceLocation STOP_S2C_ID  = rl("stop_anim_s2c");
    private static final ResourceLocation STOPALL_C2S_ID = rl("stop_all_c2s");
    private static final ResourceLocation STOPALL_S2C_ID = rl("stop_all_s2c");

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("yourmod", path);
    }

    private AnimationPacketHandler() {}

    // =================================================================
    //  РЕГИСТРАЦИЯ
    // =================================================================

    /**
     * Регистрирует все пакеты.
     * Вызывать из: modBus.addListener(AnimationPacketHandler::registerPackets);
     */
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar("1"); // версия протокола

        reg.playToServer(PlayAnimC2SPayload.TYPE, PlayAnimC2SPayload.CODEC,
                AnimationPacketHandler::handlePlayC2S);

        reg.playToClient(PlayAnimS2CPayload.TYPE, PlayAnimS2CPayload.CODEC,
                AnimationPacketHandler::handlePlayS2C);

        reg.playToServer(StopAnimC2SPayload.TYPE, StopAnimC2SPayload.CODEC,
                AnimationPacketHandler::handleStopC2S);

        reg.playToClient(StopAnimS2CPayload.TYPE, StopAnimS2CPayload.CODEC,
                AnimationPacketHandler::handleStopS2C);

        reg.playToServer(StopAllAnimC2SPayload.TYPE, StopAllAnimC2SPayload.CODEC,
                AnimationPacketHandler::handleStopAllC2S);

        reg.playToClient(StopAllAnimS2CPayload.TYPE, StopAllAnimS2CPayload.CODEC,
                AnimationPacketHandler::handleStopAllS2C);
    }

    // =================================================================
    //  ОТПРАВКА (вызывается из AnimationController)
    // =================================================================

    /** Клиент → Сервер: начать анимацию */
    public static void sendPlayToServer(AnimationDefinition def) {
        PacketDistributor.sendToServer(new PlayAnimC2SPayload(def.getLocation()));
    }

    /** Сервер → Все ближайшие клиенты: начать анимацию у игрока */
    public static void broadcastPlay(Player player, AnimationDefinition def) {
        if (!(player instanceof ServerPlayer sp)) return;
        PlayAnimS2CPayload packet = new PlayAnimS2CPayload(player.getUUID(), def.getLocation());
        // Рассылаем всем игрокам которые видят данного игрока + самому игроку
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(sp, packet);
    }

    /** Клиент → Сервер: остановить анимацию */
    public static void sendStopToServer(AnimationDefinition def) {
        PacketDistributor.sendToServer(new StopAnimC2SPayload(def.getLocation()));
    }

    /** Сервер → Все клиенты: остановить анимацию у игрока */
    public static void broadcastStop(Player player, AnimationDefinition def) {
        if (!(player instanceof ServerPlayer sp)) return;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(sp,
                new StopAnimS2CPayload(player.getUUID(), def.getLocation()));
    }

    /** Клиент → Сервер: стоп всех анимаций */
    public static void sendStopAllToServer() {
        PacketDistributor.sendToServer(new StopAllAnimC2SPayload());
    }

    /** Сервер → Все клиенты: стоп всех анимаций у игрока */
    public static void broadcastStopAll(Player player) {
        if (!(player instanceof ServerPlayer sp)) return;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(sp,
                new StopAllAnimS2CPayload(player.getUUID()));
    }

    // =================================================================
    //  ОБРАБОТЧИКИ
    // =================================================================

    // C→S: сервер получил "play" от клиента, рассылает остальным
    private static void handlePlayC2S(PlayAnimC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player sender = ctx.player();
            AnimationDefinition def = findDef(payload.animLocation());
            if (def == null || !(sender instanceof ServerPlayer sp)) return;
            // Рассылаем ДРУГИМ игрокам (не самому отправителю)
            PacketDistributor.sendToPlayersTrackingEntity(sp,
                    new PlayAnimS2CPayload(sender.getUUID(), payload.animLocation()));
        });
    }

    // S→C: клиент получил "play" от сервера, применяет локально
    private static void handlePlayS2C(PlayAnimS2CPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null) return;
            Player target = mc.level.getPlayerByUUID(payload.playerUuid());
            if (target == null) return;
            AnimationDefinition def = findDef(payload.animLocation());
            if (def == null) return;
            AnimationController.playLocal(target, def);
        });
    }

    // C→S: сервер получил "stop" от клиента
    private static void handleStopC2S(StopAnimC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player sender = ctx.player();
            if (!(sender instanceof ServerPlayer sp)) return;
            PacketDistributor.sendToPlayersTrackingEntity(sp,
                    new StopAnimS2CPayload(sender.getUUID(), payload.animLocation()));
        });
    }

    // S→C: клиент получил "stop"
    private static void handleStopS2C(StopAnimS2CPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null) return;
            Player target = mc.level.getPlayerByUUID(payload.playerUuid());
            if (target == null) return;
            AnimationDefinition def = findDef(payload.animLocation());
            if (def == null) return;
            AnimationController.stopLocal(target, def);
        });
    }

    // C→S: стоп всех
    private static void handleStopAllC2S(StopAllAnimC2SPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player sender = ctx.player();
            if (!(sender instanceof ServerPlayer sp)) return;
            PacketDistributor.sendToPlayersTrackingEntity(sp,
                    new StopAllAnimS2CPayload(sender.getUUID()));
        });
    }

    // S→C: стоп всех
    private static void handleStopAllS2C(StopAllAnimS2CPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null) return;
            Player target = mc.level.getPlayerByUUID(payload.playerUuid());
            if (target == null) return;
            AnimationController.stopAllLocal(target);
        });
    }

    // -----------------------------------------------------------------
    // Вспомогательный метод: найти дескриптор по ResourceLocation
    // -----------------------------------------------------------------

    private static AnimationDefinition findDef(ResourceLocation loc) {
        for (AnimationDefinition def : PlayerAnimationManager.getAllDefinitions()) {
            if (def.getLocation().equals(loc)) return def;
        }
        return null;
    }

    // =================================================================
    //  PAYLOAD RECORDS
    // =================================================================

    // ---- Play C→S ----
    public record PlayAnimC2SPayload(ResourceLocation animLocation)
            implements CustomPacketPayload {
        public static final Type<PlayAnimC2SPayload> TYPE = new Type<>(PLAY_C2S_ID);
        public static final StreamCodec<FriendlyByteBuf, PlayAnimC2SPayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> buf.writeResourceLocation(p.animLocation),
                        buf -> new PlayAnimC2SPayload(buf.readResourceLocation())
                );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ---- Play S→C ----
    public record PlayAnimS2CPayload(UUID playerUuid, ResourceLocation animLocation)
            implements CustomPacketPayload {
        public static final Type<PlayAnimS2CPayload> TYPE = new Type<>(PLAY_S2C_ID);
        public static final StreamCodec<FriendlyByteBuf, PlayAnimS2CPayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> { buf.writeUUID(p.playerUuid); buf.writeResourceLocation(p.animLocation); },
                        buf -> new PlayAnimS2CPayload(buf.readUUID(), buf.readResourceLocation())
                );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ---- Stop C→S ----
    public record StopAnimC2SPayload(ResourceLocation animLocation)
            implements CustomPacketPayload {
        public static final Type<StopAnimC2SPayload> TYPE = new Type<>(STOP_C2S_ID);
        public static final StreamCodec<FriendlyByteBuf, StopAnimC2SPayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> buf.writeResourceLocation(p.animLocation),
                        buf -> new StopAnimC2SPayload(buf.readResourceLocation())
                );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ---- Stop S→C ----
    public record StopAnimS2CPayload(UUID playerUuid, ResourceLocation animLocation)
            implements CustomPacketPayload {
        public static final Type<StopAnimS2CPayload> TYPE = new Type<>(STOP_S2C_ID);
        public static final StreamCodec<FriendlyByteBuf, StopAnimS2CPayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> { buf.writeUUID(p.playerUuid); buf.writeResourceLocation(p.animLocation); },
                        buf -> new StopAnimS2CPayload(buf.readUUID(), buf.readResourceLocation())
                );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ---- StopAll C→S ----
    public record StopAllAnimC2SPayload() implements CustomPacketPayload {
        public static final Type<StopAllAnimC2SPayload> TYPE = new Type<>(STOPALL_C2S_ID);
        public static final StreamCodec<FriendlyByteBuf, StopAllAnimC2SPayload> CODEC =
                StreamCodec.of((buf, p) -> {}, buf -> new StopAllAnimC2SPayload());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ---- StopAll S→C ----
    public record StopAllAnimS2CPayload(UUID playerUuid) implements CustomPacketPayload {
        public static final Type<StopAllAnimS2CPayload> TYPE = new Type<>(STOPALL_S2C_ID);
        public static final StreamCodec<FriendlyByteBuf, StopAllAnimS2CPayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> buf.writeUUID(p.playerUuid),
                        buf -> new StopAllAnimS2CPayload(buf.readUUID())
                );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}