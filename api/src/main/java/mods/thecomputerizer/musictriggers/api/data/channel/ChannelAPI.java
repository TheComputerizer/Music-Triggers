package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.io.LogHelper.ModLogger;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import org.apache.logging.log4j.Level;

@Getter
public abstract class ChannelAPI implements ChannelEventHandler, LoggableAPI {

    public static ModLogger LOGGER = LogHelper.create(MTRef.MODID);

    /**
     * Logs a channel qualified message both the normal log and MT log
     */
    public static void log(String type, String typeName, Level level, String msg, Object ... args) {
        msg = type+"["+typeName+"]: "+msg;
        MTRef.log(level,msg,args);
        ChannelAPI.LOGGER.log(level,msg,args);
    }

    private final ChannelInfo info;
    private final ChannelData data;
    private final TriggerSelectorAPI<?,?> selector;
    private final String name;
    @Setter private boolean enabled;
    protected TriggerAPI activeTrigger;

    protected ChannelAPI(Table table) {
        this.name = table.getName();
        this.info = new ChannelInfo(this,table);
        this.data = new ChannelData(this);
        this.selector = getSelector();
    }

    @Override
    public void activate() {
        for(ChannelEventHandler handler : this.data.getActiveEventHandlers()) handler.activate();
    }

    @Override
    public void deactivate() {
        for(ChannelEventHandler handler : this.data.getActiveEventHandlers()) handler.activate();
    }

    public TriggerAPI getActiveTrigger() {
        return this.selector.getActiveTrigger();
    }

    public abstract AudioPlayer getPlayer();
    protected abstract TriggerSelectorAPI<?,?> getSelector();
    public abstract boolean isClientChannel();

    @Override
    public void logAll(String msg, Object ... args) {
        log("Channel",getName(),Level.ALL,msg,args);
    }

    @Override
    public void logDebug(String msg, Object ... args) {
        log("Channel",getName(),Level.DEBUG,msg,args);
    }

    @Override
    public void logError(String msg, Object ... args) {
        log("Channel",getName(),Level.ERROR,msg,args);
    }

    @Override
    public void logFatal(String msg, Object ... args) {
        log("Channel",getName(),Level.FATAL,msg,args);
    }

    @Override
    public void logInfo(String msg, Object ... args) {
        log("Channel",getName(),Level.INFO,msg,args);
    }

    @Override
    public void logTrace(String msg, Object ... args) {
        log("Channel",getName(),Level.TRACE,msg,args);
    }

    @Override
    public void logWarn(String msg, Object ... args) {
        log("Channel",getName(),Level.WARN,msg,args);
    }

    public abstract void onTrackStart(AudioTrack track);
    public abstract void onTrackStop(AudioTrackEndReason endReason);

    public void parseData() {
        this.data.parse();
    }

    @Override
    public void play() {
        for(ChannelEventHandler handler : this.data.getActiveEventHandlers()) handler.play();
    }

    @Override
    public void playing() {
        for(ChannelEventHandler handler : this.data.getActiveEventHandlers()) handler.playing();
    }

    @Override
    public void playable() {}

    @Override
    public void queue() {
        for(ChannelEventHandler handler : this.data.getActiveEventHandlers()) handler.queue();
    }

    public abstract void setCategoryVolume(float volume);
    public abstract void setTrackVolume(float volume);

    @Override
    public void stop() {
        for(ChannelEventHandler handler : this.data.getActiveEventHandlers()) handler.stop();
    }

    @Override
    public void stopped() {
        for(ChannelEventHandler handler : this.data.getActiveEventHandlers()) handler.stopped();
    }

    @Override
    public void tickActive() {
        for(ChannelEventHandler handler : this.data.getActiveEventHandlers()) handler.stopped();
    }

    public abstract void tickFast();

    @Override
    public void tickPlayable() {}

    public abstract void tickSlow();

    @Override
    public void unplayable() {}
}