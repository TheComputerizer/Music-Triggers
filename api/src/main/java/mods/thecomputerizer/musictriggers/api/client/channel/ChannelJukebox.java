package mods.thecomputerizer.musictriggers.api.client.channel;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import mods.thecomputerizer.musictriggers.api.client.audio.AudioContainer;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.Objects;

public final class ChannelJukebox extends ChannelClient {
    
    private String playingName;
    private AudioTrack playingTrack;
    
    public ChannelJukebox(ChannelHelper helper, Toml table) {
        super(helper,table);
        this.setMasterVolume(1f);
        this.setCategoryVolume(1f);
        this.setTrackVolume(1f);
    }
    
    @Override
    public String getPlayingSongName() {
        return this.playingName;
    }
    
    public boolean isPlaying() {
        return Objects.nonNull(this.playingTrack);
    }
    
    public void playReference(AudioRef ref) {
        if(ref instanceof AudioContainer) {
            AudioContainer container = (AudioContainer)ref;
            AudioTrack track = container.checkState(container.getTrack());
            if(Objects.nonNull(track)) {
                this.playingName = container.getName();
                this.player.playTrack(track);
                this.playingTrack = track;
            } else logError("Cannot play track null track from {}!",container);
        } else logError("Cannot play track from non audio container!");
    }
    
    public void stop() {
        this.player.stopTrack();
        this.playingTrack = null;
        this.playingName = null;
    }
    
    @Override
    public void tickActive() {}
    
    @Override
    public void tickPlayable() {}
    
    @Override
    public void tickSlow() {}
}