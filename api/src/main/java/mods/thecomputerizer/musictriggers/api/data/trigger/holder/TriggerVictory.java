package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerVictory extends HolderTrigger {

    public TriggerVictory(ChannelAPI channel) {
        super(channel,"victory");
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
