package mods.thecomputerizer.musictriggers.api.server;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.util.List;

public class ServerChannel extends ChannelAPI {

    public ServerChannel(Table table) {
        super(table);
    }

    @Override
    public AudioPlayer getPlayer() {
        return null;
    }

    @Override
    public boolean isClientChannel() {
        return false;
    }

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {}

    @Override
    public void tickFast() {

    }

    @Override
    public void tickSlow() {

    }
}
