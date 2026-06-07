package net.iwtengu.shinobiway.component;

import net.iwtengu.shinobiway.ShinobiWay;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ShinobiWay.MOD_ID);

    // Компонент владельца глаза
    public static final Supplier<DataComponentType<EyeOwnerComponent>> EYE_OWNER =
            DATA_COMPONENTS.register("eye_owner", () ->
                    DataComponentType.<EyeOwnerComponent>builder()
                            .persistent(EyeOwnerComponent.CODEC)
                            .networkSynchronized(EyeOwnerComponent.STREAM_CODEC)
                            .build()
            );

    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}