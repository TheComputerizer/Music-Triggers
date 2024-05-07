package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerVictory extends HolderTrigger {

    public TriggerVictory(ChannelAPI channel) {
        super(channel,"victory");
    }
    
    @Override
    public boolean imply(String id) {
        try {
            int persistence = Integer.parseInt(id);
            if(persistence<=0) throw new NumberFormatException("Integer must be positive");
            setExistingParameterValue("persistence",persistence);
        } catch(NumberFormatException ex) {
            logError("Failed to imply persistance from identifier {} (must be a positive integer)",id,ex);
        }
        return super.imply(id);
    }
    
    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveVictory(getParameterAsInt("victory_timeout"));
    }

    @Override
    public boolean isServer() {
        return true;
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","persistence"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
