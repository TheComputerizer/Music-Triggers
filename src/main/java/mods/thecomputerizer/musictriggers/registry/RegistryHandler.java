package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.server.TriggerCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class RegistryHandler {

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent ev) {
        TriggerCommand.register(ev.getDispatcher());
    }

    public static void init(IEventBus eventBus) {
        Constants.MAIN_LOG.info("Loading Music Triggers discs and blocks");
        BlockRegistry.registerBlocks(eventBus);
        ItemRegistry.registerItems(eventBus);
        TileRegistry.registerTiles(eventBus);
    }
}
