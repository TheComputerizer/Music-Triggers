package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;

import java.util.Collections;
import java.util.Map;

public class TriggerInventory extends HolderTrigger {

    public TriggerInventory(ChannelAPI channel) {
        super(channel,"inventory");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"items",new ParameterList<>(String.class,Collections.singletonList("EMPTY")));
        addParameter(map,"slots",new ParameterList<>(String.class,Collections.singletonList("ANY")));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
