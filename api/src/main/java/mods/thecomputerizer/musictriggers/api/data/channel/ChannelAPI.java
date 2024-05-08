package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.TriggerContextClient;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelector;
import mods.thecomputerizer.musictriggers.api.network.MessageInitChannels.ChannelMessage;
import mods.thecomputerizer.musictriggers.api.server.TriggerContextServer;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public abstract class ChannelAPI implements ChannelEventHandler, LoggableAPI {

    private final ChannelHelper helper;
    private final ChannelInfo info;
    private final ChannelData data;
    private final ChannelSync sync;
    private final TriggerSelector selector;
    private final String name;
    @Setter private boolean enabled;
    private int ticks;

    protected ChannelAPI(ChannelHelper helper, Toml table) {
        this.helper = helper;
        this.name = table.getName();
        this.info = new ChannelInfo(this,table);
        this.data = new ChannelData(this);
        this.sync = new ChannelSync(this);
        this.selector = new TriggerSelector(this,helper.isClient() ?
                new TriggerContextClient(this) : new TriggerContextServer(this));
    }

    @Override
    public void activate() {
        TriggerAPI activeTrigger = getActiveTrigger();
        if(Objects.nonNull(activeTrigger)) logDebug("Activated {}",activeTrigger);
        handleActiveEvent(ChannelEventHandler::activate);
    }

    public abstract boolean checkDeactivate(TriggerAPI current, TriggerAPI next);

    /**
     * Stops any audio that is playing & clears all data
     */
    public void close() {
        this.data.close();
        this.sync.close();
        this.selector.close();
        this.ticks = 0;
    }

    @Override
    public void deactivate() {
        TriggerAPI activeTrigger = getActiveTrigger();
        if(Objects.nonNull(activeTrigger)) logDebug("Deactivated {}",activeTrigger);
        handleActiveEvent(ChannelEventHandler::deactivate);
    }

    public TriggerAPI getActiveTrigger() {
        return this.selector.getActiveTrigger();
    }

    public Collection<TriggerAPI> getPlayableTriggers() {
        Set<TriggerAPI> triggers = new HashSet<>();
        for(TriggerAPI trigger : this.data.getTriggers())
            if(trigger.getState().isPlayable()) triggers.add(trigger);
        return triggers;
    }

    public abstract AudioPlayer getPlayer();
    
    @SuppressWarnings("unchecked")
    public <P> @Nullable PlayerAPI<P,?> getPlayerEntity() {
        return (PlayerAPI<P,?>)this.selector.getContext().getPlayer();
    }
    
    public Set<String> getRecordLines() {
        Set<String> lines = new HashSet<>();
        for(RecordElement record : this.data.getRecords()) lines.add(record.toString());
        return lines;
    }
    
    public Set<String> getRedirectLines() {
        Set<String> lines = new HashSet<>();
        for(RedirectElement redirect : this.data.getRedirects()) lines.add(redirect.toString());
        return lines;
    }
    
    public void getSource(Map<String,Toml> map, String name, String path) {
        Toml toml = ChannelHelper.openToml(MTRef.CONFIG_PATH+"/"+this.name+"/"+path, false, this);
        if(Objects.nonNull(toml)) map.put(name,toml);
    }
    
    public Map<String,Toml> getSourceMap() {
        Map<String,Toml> map = new HashMap<>();
        getSource(map,"commands",this.info.getCommandsPath());
        getSource(map,"main",this.info.getMainPath());
        getSource(map,"renders",this.info.getRendersPath());
        return map;
    }
    
    protected abstract String getTypeName();

    protected void handleActiveEvent(Consumer<ChannelEventHandler> event) {
        this.data.getActiveEventHandlers().forEach(event);
    }

    protected void handlePlayableEvent(Consumer<ChannelEventHandler> event) {
        this.data.getPlayableEventHandlers().forEach(event);
    }
    
    public boolean implyTrigger(String name) {
        int index = name.indexOf('-');
        String id = index!=-1 ? name.substring(index+1) : "implied";
        name = index!=-1 ? name.substring(0,index) : name;
        return this.data.implyTrigger(name,id);
    }

    public abstract boolean isClientChannel();
    public abstract boolean isValid();
    
    public void loadData(ChannelMessage message) {
        this.data.load(message);
    }

    public abstract void loadLocalTrack(AudioRef ref, String location);
    public abstract void loadRemoteTrack(AudioRef ref, String location);
    
    public void loadTracks(boolean loadResources) {
        this.data.loadTracks(loadResources);
    }

    @Override
    public void logAll(String msg, Object ... args) {
        MTLogger.logAll(getTypeName(),getName(),msg,args);
    }

    @Override
    public void logDebug(String msg, Object ... args) {
        MTLogger.logDebug(getTypeName(),getName(),msg,args);
    }

    @Override
    public void logError(String msg, Object ... args) {
        MTLogger.logError(getTypeName(),getName(),msg,args);
    }

    @Override
    public void logFatal(String msg, Object ... args) {
        MTLogger.logFatal(getTypeName(),getName(),msg,args);
    }

    @Override
    public void logInfo(String msg, Object ... args) {
        MTLogger.logInfo(getTypeName(),getName(),msg,args);
    }

    @Override
    public void logTrace(String msg, Object ... args) {
        MTLogger.logTrace("Channel",getName(),msg,args);
    }

    @Override
    public void logWarn(String msg, Object ... args) {
        MTLogger.logWarn(getTypeName(),getName(),msg,args);
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
    public abstract boolean shouldBlockMusicTicker();

    @Override
    public void stop() {
        handleActiveEvent(ChannelEventHandler::stop);
    }

    @Override
    public void stopped() {
        logInfo("Stopped track");
        handleActiveEvent(ChannelEventHandler::stopped);
    }

    public void sync() {
        this.sync.send();
    }

    public void tick() {
        tickActive();
        tickPlayable();
        if((this.ticks++)%getHelper().getDebugNumber("slow_tick_factor").intValue()==0) tickSlow();
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
        sync();
        this.ticks = 0;
    }
    
    @Override
    public String toString() {
        return getTypeName()+"["+this.name+"]";
    }

    @Override
    public void unplayable() {}
}