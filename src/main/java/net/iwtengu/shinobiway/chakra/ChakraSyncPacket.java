package net.iwtengu.shinobiway.chakra;

import net.iwtengu.shinobiway.ShinobiWay;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * ╔══════════════════════════════════════════════════════════╗
 *  ChakraSyncPacket — синхронизирует данные чакры
 *  с сервера на клиент.
 *
 *  Вызывай после каждого изменения чакры на сервере:
 *    ChakraSyncPacket.send((ServerPlayer) player);
 *
 *  ── КАК ПОДКЛЮЧИТЬ ──────────────────────────────────────────
 *  В главном классе мода (в конструкторе или init):
 *    modEventBus.addListener(ChakraSyncPacket::register);
 * ╚══════════════════════════════════════════════════════════╝
 *
 * @param current   текущая чакра
 * @param max       максимальная чакра
 * @param unlocked  разблокирована ли чакра
 */
@EventBusSubscriber(modid = ShinobiWay.MOD_ID)
public record ChakraSyncPacket(float current, float max, boolean unlocked)
        implements CustomPacketPayload {

    // ── Идентификатор пакета ────────────────────────────────
    public static final Type<ChakraSyncPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            ShinobiWay.MOD_ID,
                            "chakra_sync"
                    )
            );

    // ── Кодек для сериализации пакета ───────────────────────
    // В 1.21.1 используется RegistryFriendlyByteBuf
    public static final StreamCodec<RegistryFriendlyByteBuf, ChakraSyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeFloat(pkt.current());
                        buf.writeFloat(pkt.max());
                        buf.writeBoolean(pkt.unlocked());
                    },
                    buf -> new ChakraSyncPacket(
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readBoolean()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // ════════════════════════════════════════════════════════
    //  РЕГИСТРАЦИЯ ПАКЕТА
    // ════════════════════════════════════════════════════════

    /**
     * Регистрируем пакет на MOD шине.
     *
     * Добавь в главный класс мода:
     *   modEventBus.addListener(ChakraSyncPacket::register);
     */
    public static void register(RegisterPayloadHandlersEvent event) {

        // versioned("1.0") обязателен/желателен в новых версиях NeoForge
        PayloadRegistrar registrar =
                event.registrar(ShinobiWay.MOD_ID)
                        .versioned("1.0");

        registrar.playToClient(
                TYPE,
                STREAM_CODEC,
                ChakraSyncPacket::handle
        );
    }

    // ════════════════════════════════════════════════════════
    //  СИНХРОНИЗАЦИЯ ПРИ ВХОДЕ В МИР
    // ════════════════════════════════════════════════════════

    /**
     * Когда игрок входит в мир —
     * отправляем ему актуальные ChakraData.
     *
     * Без этого после перезахода HUD не знает,
     * разблокирована ли чакра.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        // Только серверный игрок
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Синхронизация chakra data клиенту
        ChakraSyncPacket.send(player);
    }

    // ════════════════════════════════════════════════════════
    //  ОБРАБОТКА НА КЛИЕНТЕ
    // ════════════════════════════════════════════════════════

    /**
     * Вызывается на клиенте при получении пакета.
     * Обновляет локальный ChakraData клиентского игрока.
     */
    private static void handle(ChakraSyncPacket pkt, IPayloadContext ctx) {

        // enqueueWork заменяет старый workHandler().submitAsync()
        ctx.enqueueWork(() -> {

            // Получаем клиент Minecraft
            Minecraft mc = Minecraft.getInstance();

            // Проверяем что игрок существует
            if (mc.player != null) {

                // Получаем attachment чакры
                ChakraData data = ChakraAttachment.get(mc.player);

                // Обновляем значения
                data.setCurrent(pkt.current());
                data.setMax(pkt.max());

                // Обновляем статус разблокировки
                if (pkt.unlocked()) {
                    data.unlock();
                } else {
                    data.lock();
                }
            }
        });
    }

    // ════════════════════════════════════════════════════════
    //  УТИЛИТНЫЙ МЕТОД ОТПРАВКИ
    // ════════════════════════════════════════════════════════

    /**
     * Отправить актуальные данные чакры конкретному игроку.
     *
     * Использование:
     *   ChakraSyncPacket.send((ServerPlayer) player);
     *
     * Вызывай после ЛЮБОГО изменения чакры на сервере.
     */
    public static void send(ServerPlayer player) {

        // Получаем chakra attachment игрока
        ChakraData data = ChakraAttachment.get(player);

        // Отправляем пакет клиенту
        PacketDistributor.sendToPlayer(
                player,
                new ChakraSyncPacket(
                        data.getCurrent(),
                        data.getMax(),
                        data.isUnlocked()
                )
        );
    }
}