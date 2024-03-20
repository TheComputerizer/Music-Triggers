package mods.thecomputerizer.musictriggers.api;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class MTAPI {

    public abstract TriggerContextAPI<?,?> getTriggerContext(ChannelAPI channel);
    public abstract <P,W> TriggerSelectorAPI<P,W> getTriggerSelector(ChannelAPI channel, TriggerContextAPI<P,W> context);

    public void init() throws IOException {
        File configDir = new File(MTRef.CONFIG_PATH);
        if(!configDir.exists() && !configDir.mkdirs())
            throw new FileNotFoundException("Unable to create file directory at "+MTRef.CONFIG_PATH+"! Music Triggers "+
                    "is unable to load any further.");
    }
}