package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;

import java.util.Map;

public class TriggerTime extends HolderTrigger {

    public TriggerTime(ChannelAPI channel) {
        super(channel,"time");
    }
    
    @Override
    public boolean imply(String id) {
        if(Misc.equalsAny(id,"day","night","sunrise","sunset"))
            setExistingParameterValue("time_bundle",id);
        else logError("Unable to imply time_bundle from identifier {} (accepts day,night,sunrise,sunset)",id);
        return super.imply(id);
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        super.initExtraParameters(map);
        addParameter(map,"end_hour",new ParameterFloat(0f));
        addParameter(map,"highest_day_number",new ParameterInt(Integer.MAX_VALUE));
        addParameter(map,"lowest_day_number",new ParameterInt(0));
        addParameter(map,"moon_phase",new ParameterInt(0));
        addParameter(map,"start_hour",new ParameterFloat(0f));
        addParameter(map,"time_bundle",new ParameterString("any"));
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveTime(getParameterAsString("time_bundle"),getParameterAsFloat("start_hour"),
                getParameterAsFloat("end_hour"),getParameterAsInt("lowest_day_number"),
                getParameterAsInt("highest_day_number"),getParameterAsInt("moon_phase"));
    }

    @Override
    public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"time_bundle","start_hour"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
