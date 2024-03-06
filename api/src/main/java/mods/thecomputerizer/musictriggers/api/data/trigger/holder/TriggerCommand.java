package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class TriggerCommand extends HolderTrigger {

    public TriggerCommand(ChannelAPI channel) {
        super(channel,"command");
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    protected boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","persistence"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
