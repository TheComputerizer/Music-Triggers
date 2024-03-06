package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.channel.IChannel;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TriggerSeason extends HolderTrigger {

    public TriggerSeason(IChannel channel) {
        super(channel,"season");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("sereneseasons");
    }


    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"level",new ParameterInt(0));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
