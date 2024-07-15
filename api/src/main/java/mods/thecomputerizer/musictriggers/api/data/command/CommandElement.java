package mods.thecomputerizer.musictriggers.api.data.command;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElementRunner;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.ServerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.COMMAND;

@Getter
public class CommandElement extends ChannelElementRunner {

    @Getter private static final List<String> headerLines = Arrays.asList("# What are you looking at!?","# ...","# ...?");
    
    public static CommandElement addToGui(MTScreenInfo info) {
        return new CommandElement(info.getChannel(),Toml.getEmpty(),true);
    }

    private final boolean valid;
    private String literal;
    private final List<TriggerAPI> triggers;
    private final boolean silent;
    
    public CommandElement(ChannelAPI channel, Toml table) {
        this(channel,table,false);
    }

    CommandElement(ChannelAPI channel, Toml table, boolean silent) {
        super(channel,"command_element");
        this.triggers = new ArrayList<>();
        this.silent = silent;
        this.valid = parse(table);
    }
    
    @Override public boolean isClient() {
        return false;
    }
    
    @Override public boolean isServer() {
        return true;
    }
    
    @Override
    public void close() {
        this.literal = null;
        this.triggers.clear();
    }

    @Override
    public boolean isResource() {
        return false;
    }

    @Override public boolean parse(Toml table) {
        return super.parse(table) && parseTriggers(this.channel,this.triggers);
    }
    
    @Override public TableRef getReferenceData() {
        return COMMAND;
    }
    
    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return CommandElement.class;
    }
    
    @Override protected String getSubTypeName() {
        return "Command";
    }

    @Override
    public boolean verifyRequiredParameters() {
        if(hasParameter("literal")) {
            this.literal = getParameterAsString("literal");
            return true;
        }
        logMissingParameter("literal");
        return false;
    }

    @Override
    public void run() {
        super.run();
        ServerHelper.executeCommandLiteral(this.literal);
    }
}