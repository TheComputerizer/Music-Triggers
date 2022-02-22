package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.MusicTriggersBlocks;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import mods.thecomputerizer.musictriggers.config.configRegistry;
import net.minecraftforge.eventbus.api.IEventBus;


public final class RegistryHandler {

    public static void registerItems(IEventBus eventBus) {
        MusicTriggersItems.INSTANCE.init();
        if(configRegistry.registerDiscs) {
            MusicTriggers.logger.info("Loading Items from Music Triggers");
            MusicTriggersBlocks.INSTANCE.initItem();
            MusicTriggersItems.ITEMS.register(eventBus);
            MusicTriggersBlocks.BLOCK_ITEMS.register(eventBus);
        }
    }

    public static void registerSoundEvents(IEventBus eventBus)
    {
        MusicTriggers.logger.info("Loading Sounds from Music Triggers");
        ModSounds.INSTANCE.init();
        if(configRegistry.registerDiscs) {
            ModSounds.SOUNDS.register(eventBus);
        }
    }

    public static void registerBlocks(IEventBus eventBus) {
        MusicTriggers.logger.info("Loading Blocks from Music Triggers");
        MusicTriggersBlocks.INSTANCE.initBlock();
        MusicTriggersBlocks.BLOCKS.register(eventBus);
    }

    public static void init(IEventBus eventBus) {
        if(configRegistry.registerDiscs) {
            registerBlocks(eventBus);
        }
        registerItems(eventBus);
        registerSoundEvents(eventBus);
    }
}
