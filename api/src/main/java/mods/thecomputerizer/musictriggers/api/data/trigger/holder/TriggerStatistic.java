package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Collections;
import java.util.Map;

public class TriggerStatistic extends HolderTrigger {

    public TriggerStatistic(ChannelAPI channel) {
        super(channel,"statistic");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"level",new ParameterInt(0));
        addParameter(map,"resource_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    protected boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","resource_name"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
