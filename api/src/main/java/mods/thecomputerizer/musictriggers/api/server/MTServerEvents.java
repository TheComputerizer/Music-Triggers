package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageRequestChannels;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.EventHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.PlayerLoggedInEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.PlayerLoggedOutEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.RegisterCommandsEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.server.event.events.ServerTickEventWrapper;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.PLAYER_LOGGED_OUT;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.types.CommonTickableEventType.TickPhase.END;
import static mods.thecomputerizer.theimpossiblelibrary.api.server.event.ServerEventWrapper.ServerType.TICK_SERVER;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.PLAYER_LOGGED_IN;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.REGISTER_COMMANDS;

public class MTServerEvents {
    
    static int ticksUntilReload;

    public static void init() {
        MTRef.logInfo("Initializing server event invokers");
        EventHelper.addListener(PLAYER_LOGGED_IN,MTServerEvents::onPlayerJoin);
        EventHelper.addListener(PLAYER_LOGGED_OUT,MTServerEvents::onPlayerLeave);
        EventHelper.addListener(REGISTER_COMMANDS,MTServerEvents::onRegisterCommands);
        EventHelper.addListener(TICK_SERVER,MTServerEvents::onServerTick);
    }
    
    public static void onServerTick(ServerTickEventWrapper<?> wrapper) {
        if(wrapper.isPhase(END)) {
            if(ticksUntilReload>=0) {
                if(ticksUntilReload==0) ChannelHelper.reload();
                ticksUntilReload--;
            }
        }
    }
    
    public static void onPlayerJoin(PlayerLoggedInEventWrapper<?> wrapper) {
        PlayerAPI<?,?> player = wrapper.getPlayer();
        String uuid = player.getUUID().toString();
        MTRef.logInfo("Found joining player with UUID {}",uuid);
        MTNetwork.sendToClient(new MessageRequestChannels<>(uuid,false),false,player);
    }
    
    public static void onPlayerLeave(PlayerLoggedOutEventWrapper<?> wrapper) {
        PlayerAPI<?,?> player = wrapper.getPlayer();
        String uuid = player.getUUID().toString();
        MTRef.logInfo("Found leaving player with UUID {}",uuid);
        ChannelHelper.closePlayerChannel(uuid);
    }

    public static void onRegisterCommands(RegisterCommandsEventWrapper<?> wrapper) {
        MTRef.logInfo("Registering commands");
        wrapper.registerCommand(MTCommands.root(MODID));
        wrapper.registerCommand(MTCommands.root("mt"));
    }
    
    public static void queueServerReload(int ticks) {
        ChannelHelper.onReloadQueued(false);
        ticksUntilReload = ticks;
    }
}