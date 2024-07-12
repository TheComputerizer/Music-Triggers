package mods.thecomputerizer.musictriggers.api.client.channel;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.client.audio.AudioContainer;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.shadow.org.joml.Vector3i;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.vectors.VectorHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import org.apache.logging.log4j.Level;

import java.util.Objects;

import static org.apache.logging.log4j.Level.*;

public abstract class ChannelClientSpecial extends ChannelClient {
    
    @Setter protected Level logLevel;
    protected String playingName;
    protected Vector3i playingPos = VectorHelper.max3I();
    protected AudioTrack playingTrack;
    
    public ChannelClientSpecial(ChannelHelper helper, Toml info) {
        this(helper,info,WARN);
    }
    
    public ChannelClientSpecial(ChannelHelper helper, Toml info, Level logLevel) {
        super(helper,info);
        this.logLevel = logLevel;
    }
    
    public abstract void checkStop(Vector3i pos);
    
    @Override public String getPlayingSongName() {
        return this.playingName;
    }
    
    public boolean isPlaying() {
        return Objects.nonNull(this.playingTrack);
    }
    
    @Override public void loadTracks(boolean ignored) {}
    
    @Override public void logDebug(String msg, Object ... args) {
        if(Objects.nonNull(this.logLevel) && this.logLevel.intLevel()>=DEBUG.intLevel()) super.logDebug(msg,args);
    }
    
    @Override public void logError(String msg, Object ... args) {
        if(Objects.isNull(this.logLevel) || this.logLevel.intLevel()>=ERROR.intLevel()) super.logError(msg, args);
    }
    
    @Override public void logFatal(String msg, Object ... args) {
        if(Objects.isNull(this.logLevel) || this.logLevel.intLevel()>=FATAL.intLevel()) super.logFatal(msg, args);
    }
    
    @Override public void logInfo(String msg, Object ... args) {
        if(Objects.isNull(this.logLevel) || this.logLevel.intLevel()>=INFO.intLevel()) super.logInfo(msg, args);
    }
    
    @Override public void logTrace(String msg, Object ... args) {
        if(Objects.nonNull(this.logLevel) && this.logLevel.intLevel()>=TRACE.intLevel()) super.logTrace(msg,args);
    }
    
    @Override public void logWarn(String msg, Object ... args) {
        if(Objects.isNull(this.logLevel) || this.logLevel.intLevel()>=WARN.intLevel()) super.logWarn(msg, args);
    }
    
    @Override public void parseData() {}
    
    public void playReference(AudioRef ref, Vector3i pos) {
        if(ref instanceof AudioContainer) {
            AudioContainer container = (AudioContainer)ref;
            AudioTrack track = container.getTrack();
            if(Objects.nonNull(track)) {
                this.playingName = container.getName();
                this.player.playTrack(track);
                this.playingTrack = track;
                this.playingPos = pos;
            } else logError("Cannot play null track from {}!",container);
        } else logError("Cannot play track from non audio container!");
    }
    
    @Override public boolean showDebugTriggerInfo() {
        return false;
    }
    
    @Override public void stop() {
        this.player.stopTrack();
        this.playingTrack = null;
        this.playingName = null;
        this.playingPos = VectorHelper.max3I();
    }
    
    @Override public void tickActive() {}
    
    @Override public void tickPlayable() {}
    
    @Override public void tickSlow() {}
}
