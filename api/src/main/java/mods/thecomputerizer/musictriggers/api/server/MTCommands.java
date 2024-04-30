package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MessageReload;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageWrapperAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandSenderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;

import java.util.Objects;

import static mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI.ArgType.ROOT;

public class MTCommands extends CommandAPI {

    public static MTCommands root() {
        return new MTCommands("mtreload",null,ROOT,true);
    }

    public MTCommands(String name, CommandAPI parent, ArgType type, boolean executionNode) {
        super(name,parent,type,executionNode);
    }

    @Override
    public void execute(MinecraftServerAPI<?> server, CommandSenderAPI<?> sender, String... strings) throws Exception {
        ChannelHelper.getGlobalData().logInfo("Executing command on server");
        MessageWrapperAPI<?,?> wrapper = NetworkHelper.wrapMessage(NetworkHelper.getDirToClient(),new MessageReload<>());
        if(Objects.nonNull(wrapper)) sendMsg(wrapper,sender);
    }

    @Override
    public void prepareExceptionInfo() {}

    @SuppressWarnings("unchecked")
    public <P> void sendMsg(MessageWrapperAPI<P,?> wrapper, CommandSenderAPI<?> sender) {
        ChannelHelper.getGlobalData().logInfo("Sending packet");
        wrapper.setPlayer(((CommandSenderAPI<P>)sender).getSender()).send();
    }
}