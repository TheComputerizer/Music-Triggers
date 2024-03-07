package mods.thecomputerizer.musictriggers.api.data.trigger.simple;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.Collections;
import java.util.List;

public class TriggerLightRain extends SimpleTrigger {

    public TriggerLightRain(ChannelAPI channel) {
        super(channel,"lightrain");
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("betterweather");
    }

    @Override
    public boolean isActive(TriggerContextAPI ctx) {
        return ctx.isActiveLightRain();
    }
}
