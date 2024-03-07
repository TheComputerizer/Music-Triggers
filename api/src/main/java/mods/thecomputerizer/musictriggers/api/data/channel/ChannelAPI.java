package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper.ModLogger;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import org.apache.logging.log4j.Level;

import java.util.List;

@Getter
public abstract class ChannelAPI {

    public static ModLogger LOGGER = LogHelper.create(MTRef.MODID);

    private final ChannelInfo info;
    private final String name;
    @Setter private boolean enabled;

    protected ChannelAPI(Table table) {
        this.name = table.getName();
        this.info = new ChannelInfo(this,table);
    }

    public abstract List<AudioRef> getAudio();
    public abstract AudioPlayer getPlayer();
    public abstract List<TriggerAPI> getTriggers();

    public void log(Level level, String msg, Object ... args) {
        this.info.log(level,msg,args);
    }

    public void logAll(String msg, Object ... args) {
        this.info.logAll(msg,args);
    }

    public void logDebug(String msg, Object ... args) {
        this.info.logDebug(msg,args);
    }

    public void logError(String msg, Object ... args) {
        this.info.logError(msg,args);
    }

    public void logFatal(String msg, Object ... args) {
        this.info.logFatal(msg,args);
    }

    public void logInfo(String msg, Object ... args) {
        this.info.logInfo(msg,args);
    }

    public void logTrace(String msg, Object ... args) {
        this.info.logTrace(msg,args);
    }

    public void logWarn(String msg, Object ... args) {
        this.info.logWarn(msg,args);
    }

    public abstract void onTrackStop(AudioTrackEndReason endReason);
    public abstract void tickFast();
    public abstract void tickSlow();
}