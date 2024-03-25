package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.Map;

public class TriggerHeight extends HolderTrigger {

    public TriggerHeight(ChannelAPI channel) {
        super(channel,"height");
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"check_above_level",new ParameterBoolean(false));
        addParameter(map,"check_for_sky",new ParameterBoolean(true));
        addParameter(map,"level",new ParameterInt(7));
    }

    @Override
    public boolean isPlayableContext(TriggerContextAPI<?,?> ctx) {
        return ctx.isActiveHeight(getParameterAsInt("level"),getParameterAsBoolean("check_for_sky"),
                getParameterAsBoolean("check_above_level"));
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","level"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
