package net.iwtengu.shinobiway.chakra;

import net.iwtengu.shinobiway.ShinobiWay;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * ╔══════════════════════════════════════════════════════════╗
 *  ChakraAttachment — регистрирует Data Attachment для чакры.
 *
 *  В NeoForge 1.21.1 Data Attachments — это стандартный способ
 *  хранить данные у игрока (вместо старого Capability API).
 *
 *  ── КАК ПОДКЛЮЧИТЬ ──────────────────────────────────────────
 *  В главном классе мода (в конструкторе):
 *    ChakraAttachment.register(modEventBus);
 * ╚══════════════════════════════════════════════════════════╝
 */
public class ChakraAttachment {

    // ── Регистр для AttachmentType ───────────────────────────
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ShinobiWay.MOD_ID);

    /**
     * Сам тип attachment — хранит ChakraData у каждой сущности/игрока.
     * serialize = сохраняем в NBT, чтобы данные переживали рестарт и смерть.
     */
    public static final Supplier<AttachmentType<ChakraData>> CHAKRA =
            ATTACHMENT_TYPES.register("chakra", () ->
                    AttachmentType.builder(ChakraData::new) // фабрика — создаёт новый объект по умолчанию

                            // Сериализация в NBT
                            .serialize(
                                    net.minecraft.nbt.CompoundTag.CODEC.xmap(

                                            // NBT -> ChakraData
                                            tag -> {
                                                ChakraData d = new ChakraData();
                                                d.deserializeNBT(tag);
                                                return d;
                                            },

                                            // ChakraData -> NBT
                                            ChakraData::serializeNBT
                                    )
                            )

                            // Данные сохраняются после смерти игрока
                            .copyOnDeath()

                            .build()
            );
    // ── Регистрация ──────────────────────────────────────────

    /**
     * Вызови в главном классе мода в конструкторе:
     *   ChakraAttachment.register(modEventBus);
     */
    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }

    // ════════════════════════════════════════════════════════
    //  УТИЛИТНЫЕ МЕТОДЫ — удобный доступ к чакре игрока
    // ════════════════════════════════════════════════════════

    /**
     * Получить ChakraData игрока.
     *
     * Использование:
     *   ChakraData chakra = ChakraAttachment.get(player);
     */
    public static ChakraData get(Player player) {
        return player.getData(CHAKRA.get());
    }

    // ════════════════════════════════════════════════════════
    //  ШПАРГАЛКА — готовые строки для использования в других классах
    // ════════════════════════════════════════════════════════
    //
    //  ── ПОЛУЧИТЬ ОБЪЕКТ ─────────────────────────────────────
    //  ChakraData chakra = ChakraAttachment.get(player);
    //
    //  ── РАЗБЛОКИРОВАТЬ ЧАКРУ ────────────────────────────────
    //  ChakraData chakra = ChakraAttachment.get(player);
    //  chakra.unlock();
    //  ChakraSyncPacket.send((ServerPlayer) player);
    //
    //  ── ПОТРАТИТЬ ЧАКРУ (с проверкой) ───────────────────────
    //  if (ChakraAttachment.get(player).spend(30f)) { /* успех */ }
    //
    //  ── ПОТРАТИТЬ ЧАКРУ (принудительно) ─────────────────────
    //  ChakraAttachment.get(player).remove(30f);
    //
    //  ── ВОССТАНОВИТЬ ЧАКРУ ──────────────────────────────────
    //  ChakraAttachment.get(player).add(25f);
    //
    //  ── ПОТРАТИТЬ 10% ОТ МАКСИМУМА ──────────────────────────
    //  ChakraAttachment.get(player).removePercent(0.10f);
    //
    //  ── ВОССТАНОВИТЬ 20% ОТ МАКСИМУМА ───────────────────────
    //  ChakraAttachment.get(player).addPercent(0.20f);
    //
    //  ── ПОТРАТИТЬ 15% (С ПРОВЕРКОЙ) ─────────────────────────
    //  if (ChakraAttachment.get(player).spendPercent(0.15f)) { /* успех */ }
    //
    //  ── УВЕЛИЧИТЬ МАКСИМУМ ──────────────────────────────────
    //  ChakraAttachment.get(player).addMax(50f);
    //
    //  ── УМЕНЬШИТЬ МАКСИМУМ ──────────────────────────────────
    //  ChakraAttachment.get(player).removeMax(20f);
    //
    //  ── УСТАНОВИТЬ МАКСИМУМ ─────────────────────────────────
    //  ChakraAttachment.get(player).setMax(200f);
    //
    //  ── ПОЛНОСТЬЮ ВОССТАНОВИТЬ ЧАКРУ ────────────────────────
    //  ChakraAttachment.get(player).fillToMax();
    //
    //  ── ОБНУЛИТЬ ЧАКРУ ──────────────────────────────────────
    //  ChakraAttachment.get(player).empty();
    //
    //  ── УЗНАТЬ ТЕКУЩЕЕ КОЛИЧЕСТВО ───────────────────────────
    //  float current = ChakraAttachment.get(player).getCurrent();
    //
    //  ── УЗНАТЬ МАКСИМУМ ─────────────────────────────────────
    //  float max = ChakraAttachment.get(player).getMax();
    //
    //  ── УЗНАТЬ ПРОЦЕНТ (0.0 – 1.0) ──────────────────────────
    //  float pct = ChakraAttachment.get(player).getPercent();
    //
    //  ── ПРОВЕРИТЬ РАЗБЛОКИРОВАНА ЛИ ─────────────────────────
    //  boolean unlocked = ChakraAttachment.get(player).isUnlocked();
    //
    //  !! После КАЖДОГО изменения на СЕРВЕРЕ синхронизируй клиент:
    //  ChakraSyncPacket.send((ServerPlayer) player);
    //
    // ════════════════════════════════════════════════════════
}