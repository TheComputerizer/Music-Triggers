package mods.thecomputerizer.musictriggers.api.data.command;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventRunner;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.server.ServerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public class CommandElement extends ChannelEventRunner {

    @Getter private static final List<String> headerLines = Arrays.asList("# What are you looking at!?","# ...","# ...?");

    private final boolean valid;
    private String literal;
    private final List<TriggerAPI> triggers;

    public CommandElement(ChannelAPI channel, Table table) {
        super(channel);
        this.triggers = new ArrayList<>();
        this.valid = parse(table);
    }

    @Override
    public boolean canRun(String event) {
        return !this.channel.isClientChannel() && super.canRun(event);
    }

    @Override
    public void close() {
        super.close();
        this.literal = null;
        this.triggers.clear();
    }

    @Override
    public boolean isResource() {
        return false;
    }

    private boolean parse(Table table) {
        this.literal = table.getValOrDefault("literal","literally");
        if(!TriggerHelper.findTriggers(getChannel(),this.triggers,table)) {
            logError("Failed to parse command with literal `{}`",this.literal);
            return false;
        }
        return true;
    }

    @Override
    protected Class<? extends ChannelElement> getTypeClass() {
        return CommandElement.class;
    }

    @Override
    protected String getTypeName() {
        return "Command";
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"literal",new ParameterString(""));
    }

    @Override
    public boolean verifyRequiredParameters() {
        return hasParameter("literal");
    }

    @Override
    protected void run() {
        ServerHelper.executeCommandLiteral(this.literal);
    }
}