package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerTornado extends HolderTrigger {

    public TriggerTornado(ChannelAPI channel) {
        super(channel,"tornado");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("weather2");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveTornado(getParameterAsInt("detection_range"),getParameterAsInt("level"));
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","level"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
