package mods.thecomputerizer.musictriggers.api;

import mods.thecomputerizer.musictriggers.api.client.audio.TrackLoaderAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;

import javax.annotation.Nullable;

public interface MTAPI {

    @Nullable TrackLoaderAPI getTrackLoader();
    TriggerContextAPI<?,?> getTriggerContext(ChannelAPI channel);
    <P,W> TriggerSelectorAPI<P,W> getTriggerSelector(ChannelAPI channel, TriggerContextAPI<P,W> context);
}