package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.MusicTriggersBlocks;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.minecraftforge.eventbus.api.IEventBus;


public final class RegistryHandler {

    public static void init(IEventBus eventBus) {
        MusicTriggers.logger.info("Loading Music Triggers discs and blocks");
        MusicTriggersBlocks.registerBlocks(eventBus);
        MusicTriggersItems.registerItems(eventBus);
    }
}
