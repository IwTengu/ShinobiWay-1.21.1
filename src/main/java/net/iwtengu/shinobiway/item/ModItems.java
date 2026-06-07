package net.iwtengu.shinobiway.item;

import net.iwtengu.shinobiway.ShinobiWay;
import net.iwtengu.shinobiway.item.custom.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ShinobiWay.MOD_ID);

    public static final DeferredItem<Item> SHURIKEN = ITEMS.register("shuriken",
            () -> new ShurikenItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> KUNAI = ITEMS.register("kunai",
            () -> new KunaiItem(new Item.Properties()));
    public static final DeferredItem<Item> SENBON = ITEMS.register("senbon",
            () -> new SenbonItem(new Item.Properties()));
    public static final DeferredItem<Item> EYE = ITEMS.register("eye",
            () -> new EyeItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<HeadbandItem> HEADBAND_LEAF = ITEMS.register(
            "headband_leaf",
            () -> new HeadbandItem(
                    ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/armor/headband_leaf.png"),
                    1.0  // 1 единица защиты
            )
    );

    public static final DeferredItem<HeadbandItem> HEADBAND_SAND = ITEMS.register(
            "headband_sand",
            () -> new HeadbandItem(
                    ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/armor/headband_sand.png"),
                    1.0  // 1 единица защиты
            )
    );

    public static final DeferredItem<HeadbandItem> HEADBAND_LEAF_BROKEN = ITEMS.register(
            "headband_leaf_broken",
            () -> new HeadbandItem(
                    ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/armor/headband_leaf_broken.png"),
                    1.0  // 1 единица защиты
            )
    );

    public static final DeferredItem<HeadbandItem> HEADBAND_SAND_BROKEN = ITEMS.register(
            "headband_sand_broken",
            () -> new HeadbandItem(
                    ResourceLocation.fromNamespaceAndPath(ShinobiWay.MOD_ID, "textures/armor/headband_sand_broken.png"),
                    1.0  // 1 единица защиты
            )
    );


    public static void register(IEventBus eventBus ) {
        ITEMS.register(eventBus);
    }
}
