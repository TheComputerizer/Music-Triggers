package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.MusicTriggersBlocks;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;


public final class RegistryHandler {

    public static void init() {
        Constants.MAIN_LOG.info("Loading Music Triggers discs and blocks");
        MusicTriggersBlocks.initBlocks();
        MusicTriggersItems.init();
    }
}
