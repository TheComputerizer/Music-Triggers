package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageQueryLogin;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.EventHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.PlayerLoggedInEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.RegisterCommandsEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlWritingException;

import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.REGISTER_COMMANDS;

public class MTServerEvents {

    public static void init() {
        MTRef.logInfo("Initializing server event invokers");
        EventHelper.addListener(REGISTER_COMMANDS,MTServerEvents::onRegisterCommands);
    }

    public static void onRegisterCommands(RegisterCommandsEventWrapper<?> wrapper) {
        MTRef.logInfo("Registering commands");
        wrapper.registerCommand(MTCommands.root("mtreload"));
        wrapper.registerCommand(MTCommands.root("mtdebug"));
    }
    
    public static void onPlayerJoin(PlayerLoggedInEventWrapper<?> wrapper) {
        PlayerAPI<?,?> player = wrapper.getPlayer();
        String uuid = player.getUUID().toString();
        try {
            ChannelHelper.addPlayer(uuid,false);
            MTNetwork.sendToClient(new MessageQueryLogin<>(uuid),true,player);
        } catch(TomlWritingException ex) {
            MTRef.logError("Unable to register server channel for joining player with UUID {}!",uuid,ex);
        }
    }
}