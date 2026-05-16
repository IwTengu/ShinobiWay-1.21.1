package net.iwtengu.shinobiway.item;

import net.iwtengu.shinobiway.ShinobiWay;
import net.iwtengu.shinobiway.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ShinobiWay.MOD_ID);

    public static final Supplier<CreativeModeTab> SHINOBIWAYTAB = CREATIVE_MODE_TABS.register("shinobiway_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SHURIKEN.get()))
                    .title(Component.translatable("creativetab.shinobiway_tab"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModBlocks.MEDITATION_CARPET.get());
                        output.accept(ModItems.SHURIKEN.get());
                        output.accept(ModItems.KUNAI.get());
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}