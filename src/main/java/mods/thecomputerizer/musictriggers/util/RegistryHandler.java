package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.MusicTriggersBlocks;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import net.minecraftforge.eventbus.api.IEventBus;


public final class RegistryHandler {

    public static void init(IEventBus eventBus) {
        Constants.MAIN_LOG.info("Loading Music Triggers discs and blocks");
        MusicTriggersBlocks.registerBlocks(eventBus);
        MusicTriggersItems.registerItems(eventBus);
    }
}
