package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.network.MTNetwork;
import mods.thecomputerizer.musictriggers.api.network.MessageReload;
import mods.thecomputerizer.musictriggers.api.network.MessageSkipSong;
import mods.thecomputerizer.musictriggers.api.network.MessageToggleDebugParameter;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.CommandSenderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI.ArgType.INTEGER;
import static mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI.ArgType.LITERAL;
import static mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI.ArgType.ROOT;
import static mods.thecomputerizer.theimpossiblelibrary.api.server.CommandAPI.ArgType.STRING;

public class MTCommands extends CommandAPI {

    public static MTCommands root(String name) {
        return new MTCommands(name,null,ROOT,false);
    }

    public MTCommands(String name, CommandAPI parent, ArgType type, boolean executionNode) {
        super(name,parent,type,executionNode);
        for(String typeName : new String[]{"debug","reload","skip"}) addSubCommand(typeName,this,LITERAL);
    }
    
    void addSubCommand(String typeName, CommandAPI parent, ArgType type) {
        MTSubCommand sub = new MTSubCommand(typeName,parent,type,true);
        if(Misc.equalsAny(typeName,"debug","reload","skip")) {
            if("debug".equals(typeName)) addSubCommand("parameter",sub,STRING);
            else if("reload".equals(typeName)) addSubCommand("ticks",sub,INTEGER);
        }
        addSubCommand(sub);
    }
    
    @Override public void execute(MinecraftServerAPI<?> server, CommandSenderAPI<?> sender, String input, String remaining) {
        throw new IllegalArgumentException("too few arguments");
    }
    
    @Override public List<String> getTabCompletions(MinecraftServerAPI<?> minecraftServerAPI,
            CommandSenderAPI<?> commandSenderAPI, String input, String remaining) {
        return Collections.emptyList();
    }
    
    @Override public void prepareExceptionInfo() {
        this.exceptionKey = getMessageKey("usage");
    }
    
    static class MTSubCommand extends CommandAPI {
        
        public MTSubCommand(String name, CommandAPI parent, ArgType type, boolean executionNode) {
            super(name,parent,type,executionNode);
        }
        
        @Override public void execute(MinecraftServerAPI<?> server, CommandSenderAPI<?> sender, String input, String remaining) {
            ChannelHelper.logGlobalInfo("Executing command on server with args {} {}",input,remaining);
            Object entity = unwrapEntity(sender);
            switch(getName()) {
                case "debug": {
                    ChannelHelper.logGlobalInfo("Sending debug packet");
                    MTNetwork.sendToClient(new MessageToggleDebugParameter<>(false,"enable_debug_info"),false,entity);
                    sender.sendMessage(TextHelper.getTranslated(getMessageKey("success")));
                    break;
                }
                case "parameter": {
                    ChannelHelper.logGlobalInfo("Sending debug packet");
                    MTNetwork.sendToClient(new MessageToggleDebugParameter<>(false,remaining),false,entity);
                    sender.sendMessage(TextHelper.getTranslated(getMessageKey("success"),remaining));
                    break;
                }
                case "reload": {
                    ChannelHelper.logGlobalInfo("Sending reload packet");
                    MTNetwork.sendToClient(new MessageReload<>(5),false,entity);
                    break;
                }
                case "skip": {
                    ChannelHelper.logGlobalInfo("Sending skip packet");
                    MTNetwork.sendToClient(new MessageSkipSong<>(),false,(Object)unwrapEntity(sender));
                    sender.sendMessage(TextHelper.getTranslated(getMessageKey("success")));
                    break;
                }
                case "ticks": {
                    int ticks = RandomHelper.randomInt("reload_ticks",remaining,5);
                    MTNetwork.sendToClient(new MessageReload<>(ticks),false,entity);
                    break;
                }
                default: {
                    MTRef.logError("Unknown command type {}",getName());
                    break;
                }
            }
        }
        
        @Override public List<String> getTabCompletions(MinecraftServerAPI<?> server, CommandSenderAPI<?> sender,
                String input, String remaining) {
            List<String> suggestions = new ArrayList<>(Arrays.asList("debug","reload","skip"));
            if(this.parent instanceof MTSubCommand) {
                if(input.contains(this.parent.getName()) && "parameter".equals(getName())) {
                    suggestions = ChannelHelper.getGlobalData().getDebug().getBooleanParameterNames();
                    if(!remaining.isEmpty()) suggestions.removeIf(s -> !s.startsWith(remaining));
                } else suggestions.clear();
            } else suggestions.removeIf(s -> !s.startsWith(remaining));
            MTRef.logInfo("Returning suggestions {}",suggestions);
            return suggestions;
        }
        
        @Override protected boolean isValidString(String input) {
            return !StringUtils.isBlank(input);
        }
        
        @Override public void prepareExceptionInfo() {
            if(Objects.nonNull(this.parent)) this.parent.prepareExceptionInfo();
        }
    }
}