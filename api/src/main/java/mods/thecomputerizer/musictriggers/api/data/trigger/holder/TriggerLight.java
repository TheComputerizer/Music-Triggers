package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerLight extends HolderTrigger {

    public TriggerLight(ChannelAPI channel) {
        super(channel,"light");
    }
    
    @Override
    public boolean imply(String id) {
        try {
            int level = Integer.parseInt(id);
            if(level<0 || level>15) throw new NumberFormatException("Integer must be in the bounds [0,15]");
            setExistingParameterValue("level",level);
        } catch(NumberFormatException ex) {
            logError("Failed to imply level from identifier {} (must be a bounded integer between [0,15])",id,ex);
        }
        return super.imply(id);
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveLight(getParameterAsInt("level"),getParameterAsString("light_type").toLowerCase());
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","level"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
