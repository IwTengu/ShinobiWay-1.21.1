package net.iwtengu.shinobiway.entity;

import net.iwtengu.shinobiway.entity.custom.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "shinobiway");

    // =========================
    // 🌀 SHURIKEN
    // =========================
    public static final DeferredHolder<EntityType<?>, EntityType<ShurikenEntity>> SHURIKEN =
            ENTITY_TYPES.register("shuriken",
                    () -> EntityType.Builder.<ShurikenEntity>of(ShurikenEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("shuriken"));

   public static final DeferredHolder<EntityType<?>, EntityType<KunaiEntity>> KUNAI =
           ENTITY_TYPES.register("kunai",
                    () -> EntityType.Builder.<KunaiEntity>of(KunaiEntity::new, MobCategory.MISC)
                           .sized(0.25F, 0.25F)
                         .clientTrackingRange(4)
                         .updateInterval(10)
                           .build("kunai"));


    public static final DeferredHolder<EntityType<?>, EntityType<SenbonEntity>> SENBON =
            ENTITY_TYPES.register("senbon",
                   () -> EntityType.Builder.<SenbonEntity>of(SenbonEntity::new, MobCategory.MISC)
                           .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                           .updateInterval(10)
                            .build("senbon"));

   public static final DeferredHolder<EntityType<?>, EntityType<SeatEntity>> SEAT =
            ENTITY_TYPES.register("seat",
                    () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                            .sized(0.0F, 0.0F)
                            .build("seat"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}