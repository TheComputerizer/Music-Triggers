package mods.thecomputerizer.musictriggers.api.registry;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.EventHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.RegisterBlockEntitiesEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.RegisterBlocksEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.RegisterItemsEventWrapper;

import static mods.thecomputerizer.musictriggers.api.registry.MTBlockEntityRegistry.MUSIC_RECORDER_ENTITY;
import static mods.thecomputerizer.musictriggers.api.registry.MTBlockRegistry.MUSIC_RECORDER;
import static mods.thecomputerizer.musictriggers.api.registry.MTItemRegistry.ENHANCED_MUSIC_DISC;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.REGISTER_BLOCKS;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.REGISTER_BLOCK_ENTITIES;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.REGISTER_ITEMS;

public class MTRegistryHandler {
    
    public static void init() {
        MTRef.logInfo("Initializing registry events");
        EventHelper.addListener(REGISTER_BLOCKS,MTRegistryHandler::onRegisterBlocks);
        EventHelper.addListener(REGISTER_BLOCK_ENTITIES,MTRegistryHandler::onRegisterBlockEntities);
        EventHelper.addListener(REGISTER_ITEMS,MTRegistryHandler::onRegisterItems);
    }
    
    public static void onRegisterBlocks(RegisterBlocksEventWrapper<?> wrapper) {
        wrapper.register(MUSIC_RECORDER);
    }
    public static void onRegisterBlockEntities(RegisterBlockEntitiesEventWrapper<?> wrapper) {
        wrapper.register(MUSIC_RECORDER_ENTITY);
    }
    
    public static void onRegisterItems(RegisterItemsEventWrapper<?> wrapper) {
        wrapper.register(ENHANCED_MUSIC_DISC);
        wrapper.register(MTItemRegistry.MUSIC_RECORDER);
    }
}