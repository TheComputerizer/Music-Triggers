package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

import java.util.Map;

public abstract class HolderTrigger extends TriggerAPI {

    protected HolderTrigger(ChannelAPI channel, String name) {
        super(channel,name);
    }

    @Override
    public String getNameWithID() {
        return getName()+"-"+getIdentifier();
    }

    @Override
    protected void initExtraParameters(Map<String, Parameter<?>> map) {}

    protected boolean hasValidIdentifier() {
        if(hasNonDefaultParameter("identifier")) return true;
        logMissingParameter("identifier");
        return false;
    }
}
