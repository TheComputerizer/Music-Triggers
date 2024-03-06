package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;

import java.util.Collections;
import java.util.Map;

public class TriggerAdvancement extends HolderTrigger {

    public TriggerAdvancement(ChannelAPI channel) {
        super(channel,"advancement");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"resource_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    protected boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","resource_name","persistence"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
