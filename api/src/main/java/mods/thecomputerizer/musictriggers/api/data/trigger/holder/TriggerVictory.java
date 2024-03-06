package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public class TriggerVictory extends HolderTrigger {

    public TriggerVictory(ChannelAPI channel) {
        super(channel,"victory");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"victory_timeout",new ParameterInt(20));
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isServer() {
        return true;
    }

    @Override
    protected boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","persistence"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
