package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;

import java.util.Map;

public abstract class HolderTrigger extends TriggerAPI {

    protected HolderTrigger(IChannel channel, String name) {
        super(channel,name);
    }

    @Override
    public String getNameWithID() {
        return getName()+"-"+getParameterAsString("identifier");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"identifier",new ParameterString("not_set"));
    }
}
