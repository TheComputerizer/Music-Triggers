package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TriggerGamestage extends HolderTrigger {

    public TriggerGamestage(ChannelAPI channel) {
        super(channel,"gamestage");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("gamestages");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"is_whitelist",new ParameterBoolean(true));
        addParameter(map,"resource_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
