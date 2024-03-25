package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

public class TriggerCommand extends HolderTrigger {

    public TriggerCommand(ChannelAPI channel) {
        super(channel,"command");
    }

    @Override
    public boolean isPlayableContext(TriggerContextAPI<?,?> ctx) {
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
