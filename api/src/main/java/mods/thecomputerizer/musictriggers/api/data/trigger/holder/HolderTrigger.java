package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
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
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"identifier",new ParameterString("not_set"));
    }

    protected boolean hasValidIdentifier() {
        if(hasNonDefaultParameter("identifier")) return true;
        logMissingParameter("identifier");
        return false;
    }

    @Override
    public boolean matches(TriggerAPI trigger) {
        return getName().matches(trigger.getName()) && matchesAll(trigger);
    }
}