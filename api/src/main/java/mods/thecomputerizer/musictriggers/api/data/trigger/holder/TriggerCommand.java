package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerCommand extends HolderTrigger {

    public TriggerCommand(ChannelAPI channel) {
        super(channel,"command");
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
        return ctx.isActiveCommand();
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","persistence"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
