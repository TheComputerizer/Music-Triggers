package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageReload;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandSenderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;

import static mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI.ArgType.ROOT;

public class MTCommands extends CommandAPI {

    public static MTCommands root() {
        return new MTCommands("mtreload",null,ROOT,true);
    }

    public MTCommands(String name, CommandAPI parent, ArgType type, boolean executionNode) {
        super(name,parent,type,executionNode);
    }

    @Override
    public void execute(MinecraftServerAPI<?> server, CommandSenderAPI<?> sender, String... strings) {
        ChannelHelper.getGlobalData().logInfo("Executing command on server");
        MTNetwork.sendToClient(new MessageReload<>(),false,sender.getSender());
    }

    @Override
    public void prepareExceptionInfo() {}
}