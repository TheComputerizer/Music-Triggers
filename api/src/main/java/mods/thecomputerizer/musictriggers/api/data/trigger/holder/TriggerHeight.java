package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerHeight extends HolderTrigger {

    public TriggerHeight(ChannelAPI channel) {
        super(channel,"height");
    }
    
    @Override public boolean imply(String id) {
        try {
            int level = Integer.parseInt(id);
            setExistingParameterValue("level",level);
        } catch(NumberFormatException ex) {
            logError("Failed to imply level from identifier {} (must be an integer)",id,ex);
        }
        return super.imply(id);
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveHeight(getParameterAsInt("level"),getParameterAsBoolean("check_for_sky"),
                getParameterAsBoolean("check_above_level"));
    }

    @Override public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","level"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
