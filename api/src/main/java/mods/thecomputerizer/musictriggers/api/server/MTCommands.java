package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageReload;
import mods.thecomputerizer.musictriggers.api.network.MessageToggleDebugParameter;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandSenderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;

import static mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI.ArgType.ROOT;

public class MTCommands extends CommandAPI {

    public static MTCommands root(String name) {
        return new MTCommands(name,null,ROOT,true);
    }

    public MTCommands(String name, CommandAPI parent, ArgType type, boolean executionNode) {
        super(name,parent,type,executionNode);
    }

    @Override
    public void execute(MinecraftServerAPI<?> server, CommandSenderAPI<?> sender, String... strings) {
        ChannelHelper.getGlobalData().logInfo("Executing command on server");
        if("mtreload".equals(this.name)) {
            ChannelHelper.getGlobalData().logInfo("Sending reload packet");
            MTNetwork.sendToClient(new MessageReload<>(),false,sender.getSender());
        }
        else if("mtdebug".equals(this.name)) {
            ChannelHelper.getGlobalData().logInfo("Sending debug packet");
            MTNetwork.sendToClient(new MessageToggleDebugParameter<>("enable_debug_info"),false,sender.getSender());
        }
    }

    @Override
    public void prepareExceptionInfo() {}
}