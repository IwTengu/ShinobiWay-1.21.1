package net.iwtengu.shinobiway.item;

import net.iwtengu.shinobiway.ShinobiWay;
import net.iwtengu.shinobiway.item.custom.KunaiItem;
import net.iwtengu.shinobiway.item.custom.SenbonItem;
import net.iwtengu.shinobiway.item.custom.ShurikenItem;
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



    public static void register(IEventBus eventBus ) {
        ITEMS.register(eventBus);
    }
}
