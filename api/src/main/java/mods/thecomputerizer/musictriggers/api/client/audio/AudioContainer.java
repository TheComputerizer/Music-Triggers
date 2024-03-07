package mods.thecomputerizer.musictriggers.api.client.audio;

import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class AudioContainer extends AudioRef {

    public AudioContainer(ChannelAPI channel, String name) {
        super(channel,name);
    }
}
