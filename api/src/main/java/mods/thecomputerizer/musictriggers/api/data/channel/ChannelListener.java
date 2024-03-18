package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.client.audio.AudioOutput;

import java.util.Objects;

public class ChannelListener extends AudioEventAdapter {

    private static final boolean OPENAL_TEST = false;

    private final AudioOutput audioOutputThread;
    private final ChannelAPI channel;

    public ChannelListener(ChannelAPI channel) {
        if(Objects.isNull(channel))
            throw new RuntimeException("Cannot add listener to null audio channel!");
        this.channel = channel;
        this.audioOutputThread = new AudioOutput(channel);
        channel.getPlayer().addListener(this);
        this.audioOutputThread.start();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.channel.onTrackStop(endReason);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException ex) {
        this.channel.logError("Track exception caught! Restarting audio output...");
        this.audioOutputThread.pauseAudioLoop();
        this.channel.onTrackStop(AudioTrackEndReason.LOAD_FAILED);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.channel.onTrackStart(track);
    }

    public void setPitch(float pitch) {
        this.audioOutputThread.setPitch(pitch);
    }
}