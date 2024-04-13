package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.Map;

public class TriggerAdvancement extends HolderTrigger {

    public TriggerAdvancement(ChannelAPI channel) {
        super(channel,"advancement");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"display_matcher",new ParameterString("EXACT"));
        addParameter(map,"display_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
        addParameter(map,"resource_matcher",new ParameterString("PARTIAL"));
        addParameter(map,"resource_name",new ParameterList<>(String.class,Collections.singletonList("ANY")));
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveAdvancement(getResourceCtx());
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","persistence"};
        if(hasAllNonDefaultParameter(parameters)) {
            parameters = new String[]{"display_name","resource_name"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        } else logMissingParameters(parameters);
        return false;
    }
}
