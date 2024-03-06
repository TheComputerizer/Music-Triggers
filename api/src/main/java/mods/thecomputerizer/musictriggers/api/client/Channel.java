package mods.thecomputerizer.musictriggers.api.client;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

public class Channel extends ChannelAPI {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public AudioPlayer getPlayer() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
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
