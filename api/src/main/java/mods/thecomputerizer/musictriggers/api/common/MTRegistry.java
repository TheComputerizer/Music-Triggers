package mods.thecomputerizer.musictriggers.api.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClient;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.EventHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.RegisterItemsEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.item.ItemAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.RegistryHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.item.DiscBuilderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.item.ItemBuilderAPI;

import java.util.Collections;

import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.REGISTER_ITEMS;
import static mods.thecomputerizer.theimpossiblelibrary.api.registry.item.ItemBuilderAPI.ItemType.DISC;

public class MTRegistry {
    
    public static final ItemAPI<?> MT_RECORD = getDiscBuilder("record",1)
            .setSoundName(MTRef.MODID)
            .setSoundEvent(null) //TODO
            .setTootltipFunction((stack,world) -> Collections.singleton(MTClient.getTranslated("item","record.tooltip")))
            .build();
    
    public static DiscBuilderAPI getDiscBuilder(String name, int stackSize) {
        return RegistryHelper.getHandler().makeDiscBuilder(null)
                .setRegistryName(MTRef.res(name)).setItemType(DISC).setStackSize(stackSize);
    }
    
    public static ItemBuilderAPI getItemBuilder(String name, int stackSize) {
        return RegistryHelper.makeItemBuilder().setRegistryName(MTRef.res(name)).setStackSize(stackSize);
    }
    
    public static void init() {
        MTRef.logInfo("Initializing delayed registry");
        EventHelper.addListener(REGISTER_ITEMS,MTRegistry::onRegisterItems);
    }
    
    public static void onRegisterItems(RegisterItemsEventWrapper<?> wrapper) {
        wrapper.register(MT_RECORD);
    }
}