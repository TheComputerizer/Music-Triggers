package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.EventHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.RegisterCommandsEventWrapper;

import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.REGISTER_COMMANDS;

public class MTServerEvents {

    public static void init() {
        MTRef.logInfo("Initializing server event invokers");
        EventHelper.addListener(REGISTER_COMMANDS,MTServerEvents::onRegisterCommands);
    }

    public static void onRegisterCommands(RegisterCommandsEventWrapper<?> wrapper) {
        MTRef.logInfo("Registering commands");
        wrapper.registerCommand(MTCommands.root());
    }
}