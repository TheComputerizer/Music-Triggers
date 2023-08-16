package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public interface IChannel {
    String getChannelName();
    AudioPlayer getPlayer();
    void onTrackStop(AudioTrackEndReason endReason);
}
