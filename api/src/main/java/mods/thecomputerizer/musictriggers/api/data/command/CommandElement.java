package mods.thecomputerizer.musictriggers.api.data.command;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class CommandElement extends ChannelElement {

    @Getter private static final List<String> headerLines = Arrays.asList("# What are you looking at!?","# ...","# ...?");

    private final boolean valid;
    private String literal;
    private final List<TriggerAPI> triggers;


    public CommandElement(ChannelAPI channel, Table table) {
        super(channel);
        this.triggers = new ArrayList<>();
        this.valid = parse(table);
    }

    private boolean parse(Table table) {
        this.literal = table.getValOrDefault("literal","literally");
        List<String> triggerRefs = table.getValOrDefault("triggers",new ArrayList<>());
        if(!TriggerHelper.findTriggers(getChannel(),this.triggers,triggerRefs)) {
            logError("Failed to parse command with literal `{}`",this.literal);
            return false;
        }
        return true;
    }
}