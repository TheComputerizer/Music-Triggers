package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageReload;
import mods.thecomputerizer.musictriggers.api.network.MessageSkipSong;
import mods.thecomputerizer.musictriggers.api.network.MessageToggleDebugParameter;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ArrayHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandSenderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI.ArgType.ROOT;

public class MTCommands extends CommandAPI {

    public static MTCommands root(String name) {
        return new MTCommands(name,null,ROOT,true);
    }

    public MTCommands(String name, CommandAPI parent, ArgType type, boolean executionNode) {
        super(name,parent,type,executionNode);
    }

    @Override
    public void execute(MinecraftServerAPI<?> server, CommandSenderAPI<?> sender, String ... args) {
        if(ArrayHelper.isEmpty(args)) throw new IllegalArgumentException("too few arguments");
        ChannelHelper.logGlobalInfo("Executing command on server with args {}",(Object)args);
        String type = args[0];
        if("skip".equals(type)) {
            ChannelHelper.logGlobalInfo("Sending skip packet");
            MTNetwork.sendToClient(new MessageSkipSong<>(),false,sender.getSender());
        }
        else if("reload".equals(type)) {
            ChannelHelper.logGlobalInfo("Sending reload packet");
            int ticks = RandomHelper.randomInt("reload_ticks", args.length==1 ? null : args[1],5);
            MTNetwork.sendToClient(new MessageReload<>(ticks),false,sender.getSender());
        }
        else if("debug".equals(type)) {
            ChannelHelper.logGlobalInfo("Sending debug packet");
            String parameter = args.length==1 ? "enable_debug_info" : args[1];
            MTNetwork.sendToClient(new MessageToggleDebugParameter<>(false,parameter),false,sender.getSender());
        }
    }
    
    @Override public List<String> getTabCompletions(
            MinecraftServerAPI<?> minecraftServerAPI, CommandSenderAPI<?> commandSenderAPI, String ... strings) {
        if(ArrayHelper.isEmpty(strings)) return Arrays.asList("debug","reload","skip");
        String type = strings[0];
        if(strings.length==1) {
            List<String> types = new ArrayList<>();
            if("debug".startsWith(type)) types.add("debug");
            if("reload".startsWith(type)) types.add("reload");
            if("skip".startsWith(type)) types.add("skip");
            return types;
        }
        if("debug".equals(type)) {
            String name = strings.length==2 ? strings[1] : "";
            List<String> names = ChannelHelper.getGlobalData().getDebug().getBooleanParameterNames();
            names.removeIf(s -> !s.startsWith(name));
            return names;
        }
        return Collections.emptyList();
    }
    
    @Override
    public void prepareExceptionInfo() {
        this.exceptionKey = "commands."+MTRef.MODID+".usage";
    }
}