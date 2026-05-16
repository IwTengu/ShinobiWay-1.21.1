package net.iwtengu.shinobiway.block;

import net.iwtengu.shinobiway.ShinobiWay;
import net.iwtengu.shinobiway.block.custom.MeditationCarpetBlock;
import net.iwtengu.shinobiway.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ShinobiWay.MOD_ID);

    public static final DeferredBlock<Block> MEDITATION_CARPET = registerBlock("meditation_carpet",
            () -> new MeditationCarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_CARPET)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    public static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus ) {
        BLOCKS.register(eventBus);
    }
}
