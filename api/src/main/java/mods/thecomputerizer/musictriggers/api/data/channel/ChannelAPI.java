package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public abstract class ChannelAPI {

    public abstract String getName();
    public abstract AudioPlayer getPlayer();
    public abstract boolean isEnabled();
    abstract void onTrackStop(AudioTrackEndReason endReason);
    public abstract void tickFast();
    public abstract void tickSlow();
}