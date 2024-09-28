package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerZones extends HolderTrigger {

    public TriggerZones(ChannelAPI channel) {
        super(channel,"zones");
    }
    
    @Override public boolean imply(String id) {
        logError("Trigger must be explicitly defined");
        return false;
    }

    @Override public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveZones(getParameterAsInt("zone_min_x"),getParameterAsInt("zone_min_y"),
                getParameterAsInt("zone_min_z"),getParameterAsInt("zone_max_x"),
                getParameterAsInt("zone_max_y"),getParameterAsInt("zone_max_z"));
    }

    @Override public boolean verifyRequiredParameters() {
        if(hasValidIdentifier()) {
            String[] parameters = new String[]{"zone_min_x","zone_max_x","zone_min_y","zone_max_y","zone_min_z","zone_max_z"};
            if(hasAnyNonDefaultParameter(parameters)) return true;
            logMissingPotentialParameter(parameters);
        }
        return false;
    }
}
