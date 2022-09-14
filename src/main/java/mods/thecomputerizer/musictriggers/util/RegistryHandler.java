package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.MusicTriggersBlocks;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;


public final class RegistryHandler {

    public static void init() {
        MusicTriggers.logger.info("Loading Music Triggers discs and blocks");
        MusicTriggersBlocks.initBlocks();
        MusicTriggersItems.init();
    }
}
