package mods.thecomputerizer.musictriggers.api.client;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ClientChannel extends ChannelAPI {

    public ClientChannel(Table table) {
        super(table);
    }

    @Override
    public AudioPlayer getPlayer() {
        return null;
    }

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {

    }

    @Override
    public void tickFast() {

    }

    @Override
    public void tickSlow() {

    }
}
