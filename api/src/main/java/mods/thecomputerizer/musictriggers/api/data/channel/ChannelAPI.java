package mods.thecomputerizer.musictriggers.api.data.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.TriggerContextClient;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.WrapperLink;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.nbt.NBTLoadable;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.render.CardAPI;
import mods.thecomputerizer.musictriggers.api.data.render.ImageElement;
import mods.thecomputerizer.musictriggers.api.data.render.TitleElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.Link;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelector;
import mods.thecomputerizer.musictriggers.api.server.TriggerContextServer;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.BaseTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.ListTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.TagHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public abstract class ChannelAPI implements ChannelEventHandler, LoggableAPI, NBTLoadable {

    private final ChannelHelper helper;
    private final ChannelInfo info;
    private final ChannelData data;
    private final ChannelSync sync;
    private final TriggerSelector selector;
    private final String name;
    @Setter protected boolean enabled = true;
    protected Link disabledBy;

    protected ChannelAPI(ChannelHelper helper, Toml table) {
        this.helper = helper;
        this.name = table.getName();
        this.info = new ChannelInfo(this,table);
        this.data = new ChannelData(this);
        this.sync = new ChannelSync(this);
        this.selector = new TriggerSelector(this,helper.isClient() ?
                new TriggerContextClient(this) : new TriggerContextServer(this));
        logInfo("Successfully registered");
    }

    @Override
    public void activate() {
        handleActiveEvent(ChannelEventHandler::activate);
    }
    
    public boolean areTheseActive(Collection<TriggerAPI> triggers) {
        TriggerAPI trigger = getActiveTrigger();
        return Objects.nonNull(trigger) && trigger.matches(triggers);
    }

    public abstract boolean checkDeactivate(TriggerAPI current, TriggerAPI next);

    /**
     * Stops any audio that is playing & clears all data
     */
    public void close() {
        this.data.close();
        this.sync.close();
        this.selector.close();
    }

    @Override
    public void deactivate() {
        TriggerAPI activeTrigger = getActiveTrigger();
        if(Objects.nonNull(activeTrigger)) logDebug("Deactivated {}",activeTrigger);
        handleActiveEvent(ChannelEventHandler::deactivate);
    }
    
    public void deactivateLink() {
        TriggerAPI trigger = getActiveTrigger();
        if(Objects.nonNull(trigger.getActiveLink())) {
            Link link = trigger.getActiveLink();
            link.setSnapshotInherit(getPlayingSongTime());
            link.unlink();
            trigger.setActiveLink(null);
        }
    }
    
    public void disable(Link link) {
        this.disabledBy = link;
        this.enabled = false;
    }
    
    public void enable() {
        this.enabled = true;
    }
    
    @Override
    public boolean equals(Object other) {
        if(other instanceof ChannelAPI) {
            ChannelAPI channel = (ChannelAPI)other;
            return isClientChannel()==channel.isClientChannel() && this.name.equals(channel.name);
        }
        return false;
    }

    public TriggerAPI getActiveTrigger() {
        return this.selector.getActiveTrigger();
    }
    
    public WrapperLink getCommandsLink() {
        return new WrapperLink(this.data.getCommands());
    }
    
    public abstract @Nullable String getFormattedSongTime();
    
    private String getLogPrefix() {
        return getLogType()+" | Channel";
    }
    
    public abstract String getLogType();
    
    public WrapperLink getMainLink() {
        List<ParameterWrapper> audio = new ArrayList<>();
        audio.add(this.data.getUniversals(AudioRef.class));
        audio.addAll(this.data.getAudio());
        List<ParameterWrapper> triggers = new ArrayList<>();
        triggers.add(this.data.getUniversals(TriggerAPI.class));
        triggers.addAll(this.data.getTriggers());
        return new WrapperLink(audio,triggers);
    }

    public Collection<TriggerAPI> getPlayableTriggers() {
        Set<TriggerAPI> triggers = new HashSet<>();
        for(TriggerAPI trigger : this.data.getTriggers()) {
            State state;
            if(trigger.isSynced()) state = getSelector().getContext().getSyncedState(trigger);
            else state = trigger.getState();
            if(state.isPlayable()) triggers.add(trigger);
        }
        return triggers;
    }

    public abstract AudioPlayer getPlayer();
    
    @SuppressWarnings("unchecked")
    public <P> @Nullable PlayerAPI<P,?> getPlayerEntity() {
        return (PlayerAPI<P,?>)this.selector.getContext().getPlayer();
    }
    
    public abstract @Nullable AudioPool getPlayingPool();
    public abstract @Nullable String getPlayingSongName();
    public abstract long getPlayingSongTime();
    
    public Set<String> getRecordLines() {
        Set<String> lines = new HashSet<>();
        for(RecordElement record : this.data.getRecords()) lines.add(record.getKey()+" = "+record.getValue());
        return lines;
    }
    
    public WrapperLink getRecordsLink() {
        return new WrapperLink(this.data.getRecords());
    }
    
    public Set<String> getRedirectLines() {
        Set<String> lines = new HashSet<>();
        for(RedirectElement redirect : this.data.getRedirects()) lines.add(redirect.toString());
        return lines;
    }
    
    public WrapperLink getRedirectLink() {
        return new WrapperLink(this.data.getRedirects());
    }
    
    public WrapperLink getRendersLink() {
        Set<ImageElement> images = new HashSet<>();
        Set<TitleElement> titles = new HashSet<>();
        for(CardAPI card : this.data.getCards()) {
            if(card instanceof ImageElement) images.add((ImageElement)card);
            else if(card instanceof TitleElement) titles.add((TitleElement)card);
        }
        return new WrapperLink(images,titles);
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
    
    public long getStartTime() {
        long startAt = 0L;
        if(Objects.nonNull(this.disabledBy)) {
            startAt = this.disabledBy.getSnapshotInherit();
            this.disabledBy.setSnapshotInherit(0L);
            this.disabledBy = null;
        } else {
            TriggerAPI trigger = getActiveTrigger();
            if(Objects.nonNull(trigger)) {
                Link link = trigger.getActiveLink();
                if(Objects.nonNull(link)) {
                    startAt = link.getSnapshotLink();
                    link.setSnapshotLink(0L);
                }
            }
        }
        return startAt;
    }

    protected void handleActiveEvent(Consumer<ChannelEventHandler> event) {
        this.data.getActiveEventHandlers().forEach(event);
    }

    protected void handlePlayableEvent(Consumer<ChannelEventHandler> event) {
        this.data.getPlayableEventHandlers().forEach(event);
    }
    
    @Override
    public boolean hasDataToSave() {
        for(TriggerAPI trigger : this.data.getTriggerEventMap().keySet())
            if(trigger.hasDataToSave()) return true;
        return false;
    }
    
    public boolean implyTrigger(String name) {
        int index = name.indexOf('-');
        String id = index!=-1 ? name.substring(index+1) : "implied";
        name = index!=-1 ? name.substring(0,index) : name;
        return this.data.implyTrigger(name,id);
    }

    public abstract boolean isClientChannel();
    public abstract boolean isValid();

    public abstract String loadLocalTrack(AudioRef ref, String location);
    public abstract String loadRemoteTrack(AudioRef ref, String location);
    
    public void loadTracks(boolean loadResources) {
        this.data.loadTracks(loadResources);
    }

    @Override
    public void logDebug(String msg, Object ... args) {
        MTLogger.logDebug(getLogPrefix(),getName(),msg,args);
    }

    @Override
    public void logError(String msg, Object ... args) {
        MTLogger.logError(getLogPrefix(), getName(), msg, args);
    }

    @Override
    public void logFatal(String msg, Object ... args) {
        MTLogger.logFatal(getLogPrefix(), getName(), msg, args);
    }

    @Override
    public void logInfo(String msg, Object ... args) {
        MTLogger.logInfo(getLogPrefix(), getName(), msg, args);
    }

    @Override
    public void logTrace(String msg, Object ... args) {
        MTLogger.logTrace("Channel",getName(),msg,args);
    }

    @Override
    public void logWarn(String msg, Object ... args) {
        MTLogger.logWarn(getLogPrefix(), getName(), msg, args);
    }

    public void onConnected(CompoundTagAPI<?> worldData) {
        if(worldData.contains("triggers")) {
            for(BaseTagAPI<?> based : worldData.getListTag("triggers")) {
                CompoundTagAPI<?> triggerTag = based.asCompoundTag();
                TriggerAPI trigger = TriggerHelper.decodeTrigger(this,triggerTag);
                trigger.onConnected(triggerTag);
            }
        }
    }
    
    public void onLoaded(CompoundTagAPI<?> globalData) {}
    
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
    
    @Override
    public void saveGlobalTo(CompoundTagAPI<?> globalData) {
    
    }
    
    @Override
    public void saveWorldTo(CompoundTagAPI<?> worldData) {
        ListTagAPI<?> triggersTag = TagHelper.makeListTag();
        for(TriggerAPI trigger : this.data.getTriggerEventMap().keySet()) {
            if(trigger.hasDataToSave()) {
                CompoundTagAPI<?> triggerTag = TagHelper.makeCompoundTag();
                trigger.saveWorldTo(triggerTag);
                triggersTag.addTag(triggerTag);
            }
        }
        worldData.putTag("triggers",triggersTag);
    }

    public abstract void setCategoryVolume(float volume);
    public abstract void setMasterVolume(float volume);
    public abstract void setTrackVolume(float volume);
    public abstract boolean shouldBlockMusicTicker();
    
    public boolean showDebugSongInfo() {
        return ChannelHelper.getDebugBool("show_channel_info") && ChannelHelper.getDebugBool("show_song_info");
    }
    
    public boolean showDebugTriggerInfo() {
        return ChannelHelper.getDebugBool("show_channel_info") && ChannelHelper.getDebugBool("show_trigger_info");
    }

    @Override
    public void stop() {
        handleActiveEvent(ChannelEventHandler::stop);
    }

    @Override
    public void stopped() {
        logInfo("Stopped track");
        handleActiveEvent(ChannelEventHandler::stopped);
    }

    public void tick(boolean jukebox) {
        tickActive();
        tickPlayable();
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
    }
    
    @Override
    public String toString() {
        return getLogPrefix()+"["+this.name+"]";
    }

    @Override
    public void unplayable() {}
    
    public void updateSyncedState(Map<TriggerAPI,State> stateMap) {
        TriggerContext ctx = this.selector.getContext();
        stateMap.forEach(ctx::updateSyncedState);
    }
}