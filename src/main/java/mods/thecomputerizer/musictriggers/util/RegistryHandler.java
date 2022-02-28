package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.MusicTriggersBlocks;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config.configRegistry;


public final class RegistryHandler {

    public static void registerItems() {
        MusicTriggersCommon.logger.info("Loading Items from Music Triggers");
        MusicTriggersItems.init();
    }

    public static void registerSoundEvents()
    {
        MusicTriggersCommon.logger.info("Loading Sounds from Music Triggers");
        ModSounds.init();
    }

    public static void registerBlocks() {
        MusicTriggersCommon.logger.info("Loading Blocks from Music Triggers");
        MusicTriggersBlocks.initBlocks();
    }

    public static void init() {
        SoundHandler.registerSounds();
        if(configRegistry.registerDiscs) {
            registerBlocks();
            registerItems();
            registerSoundEvents();
        }
    }
}
