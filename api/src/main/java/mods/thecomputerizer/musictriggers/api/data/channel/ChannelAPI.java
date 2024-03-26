package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTAPI;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import org.apache.logging.log4j.Level;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
public abstract class ChannelAPI implements ChannelEventHandler, LoggableAPI {

    private final ChannelInfo info;
    private final ChannelData data;
    private final TriggerSelectorAPI<?,?> selector;
    private final String name;
    @Setter private boolean enabled;
    private int ticks;

    protected ChannelAPI(Table table) {
        this.name = table.getName();
        this.info = new ChannelInfo(this,table);
        this.data = new ChannelData(this);
        this.selector = initSelector(TriggerHelper.getContext(this));
    }

    @Override
    public void activate() {
        this.selector.activate();
        handleActiveEvent(ChannelEventHandler::activate);
    }

    @Override
    public void deactivate() {
        handleActiveEvent(ChannelEventHandler::deactivate);
    }

    public TriggerAPI getActiveTrigger() {
        return this.selector.getActiveTrigger();
    }

    public Collection<TriggerAPI> getPlayableTriggers() {
        return this.selector.getPlayables();
    }

    public abstract AudioPlayer getPlayer();

    protected void handleActiveEvent(Consumer<ChannelEventHandler> event) {
        this.data.getActiveEventHandlers().forEach(event);
    }

    protected void handlePlayableEvent(Consumer<ChannelEventHandler> event) {
        this.data.getPlayableEventHandlers().forEach(event);
    }

    protected <P,W> TriggerSelectorAPI<P,W> initSelector(TriggerContextAPI<P,W> context) {
        MTAPI api = MTRef.getAPI();
        return Objects.nonNull(api) ? api.getTriggerSelector(this,context) : null;
    }

    public abstract boolean isClientChannel();

    public abstract boolean isValid();

    public void loadTracks(boolean loadResources) {
        this.data.loadTracks(loadResources);
    }

    public abstract void loadLocalTrack(AudioRef ref, String location);
    public abstract void loadRemoteTrack(AudioRef ref, String location);

    @Override
    public void logAll(String msg, Object ... args) {
        MTLogger.logAll("Channel",getName(),msg,args);
    }

    @Override
    public void logDebug(String msg, Object ... args) {
        MTLogger.logDebug("Channel",getName(),msg,args);
    }

    @Override
    public void logError(String msg, Object ... args) {
        MTLogger.logError("Channel",getName(),msg,args);
    }

    @Override
    public void logFatal(String msg, Object ... args) {
        MTLogger.logFatal("Channel",getName(),msg,args);
    }

    @Override
    public void logInfo(String msg, Object ... args) {
        MTLogger.logInfo("Channel",getName(),msg,args);
    }

    @Override
    public void logTrace(String msg, Object ... args) {
        MTLogger.logTrace("Channel",getName(),msg,args);
    }

    @Override
    public void logWarn(String msg, Object ... args) {
        MTLogger.logWarn("Channel",getName(),msg,args);
    }

    public abstract void onResourcesLoaded();

    public abstract void onTrackStart(AudioTrack track);
    public abstract void onTrackStop(AudioTrackEndReason endReason);

    public void parseData() {
        this.data.parse();
    }

    @Override
    public void play() {
        handleActiveEvent(ChannelEventHandler::play);
    }

    @Override
    public void playable() {}

    @Override
    public void playing() {
        handleActiveEvent(ChannelEventHandler::playing);
    }

    @Override
    public void queue() {
        handleActiveEvent(ChannelEventHandler::queue);
    }

    public abstract void setCategoryVolume(float volume);
    public abstract void setTrackVolume(float volume);

    @Override
    public void stop() {
        handleActiveEvent(ChannelEventHandler::stop);
    }

    @Override
    public void stopped() {
        handleActiveEvent(ChannelEventHandler::stopped);
    }

    public void tick() {
        tickActive();
        tickPlayable();
        if((this.ticks++)%ChannelHelper.getDebugNumber("SLOW_TICK_FACTOR").intValue()==0) tickSlow();
    }

    @Override
    public void tickActive() {
        handleActiveEvent(ChannelEventHandler::tickActive);
    }

    @Override
    public void tickPlayable() {
        handlePlayableEvent(ChannelEventHandler::tickPlayable);
    }

    public void tickSlow() {
        this.selector.select();
        this.ticks = 0;
    }

    @Override
    public void unplayable() {}
}