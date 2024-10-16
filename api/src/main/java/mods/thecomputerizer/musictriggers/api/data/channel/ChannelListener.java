package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.audio.AudioOutput;

import java.util.Objects;

public class ChannelListener extends AudioEventAdapter {

    private static final boolean OPENAL_TEST = false;

    private final AudioOutput audioOutputThread;
    private final ChannelAPI channel;
    private boolean closing;
    @Getter private boolean broken;

    public ChannelListener(ChannelAPI channel) {
        if(Objects.isNull(channel))
            throw new RuntimeException("Cannot add listener to null audio channel!");
        this.channel = channel;
        this.audioOutputThread = new AudioOutput(channel);
        channel.getPlayer().addListener(this);
        this.audioOutputThread.start();
    }

    public void close() {
        this.audioOutputThread.close();
        this.closing = true;
    }
    
    public void disable() {
        this.audioOutputThread.pauseAudioLoop();
    }
    
    public void enable() {
        if(!this.broken) this.audioOutputThread.unpauseAudioLoop();
    }

    @Override public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(!this.closing && !this.broken) this.channel.onTrackStop(endReason);
    }

    @Override public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException ex) {
        if(!this.closing && !this.broken) {
            this.audioOutputThread.pauseAudioLoop();
            this.broken = true;
            this.channel.logError("Track exception caught! Restarting audio output...",ex);
        }
    }

    @Override public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if(!this.closing && !this.broken) this.channel.onTrackStart(track);
    }
}