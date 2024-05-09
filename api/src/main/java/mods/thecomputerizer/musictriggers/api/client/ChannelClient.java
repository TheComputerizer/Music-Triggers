package mods.thecomputerizer.musictriggers.api.client;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.client.MTDebugInfo.Element;
import mods.thecomputerizer.musictriggers.api.client.audio.TrackLoader;
import mods.thecomputerizer.musictriggers.api.client.audio.resource.ResourceAudioSourceManager;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelListener;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.EnumHelper;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_PCM_S16_BE;
import static com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality.HIGH;

public class ChannelClient extends ChannelAPI {

    private final AudioPlayerManager manager;
    private final AudioPlayer player;
    private final ChannelListener listener;
    private final TrackLoader trackLoader;
    private boolean registeredResourceAudio;
    private float categoryVolume = 1f;
    private float trackVolume;
    private boolean queued;
    private AudioPool playingPool;
    private boolean deactivating;

    public ChannelClient(ChannelHelper helper, Toml table) {
        super(helper,table);
        this.manager = createManager();
        this.player = createPlayer();
        configure(finalizeManager());
        this.listener = new ChannelListener(this);
        this.trackLoader = new TrackLoader();
        logInfo("Successfully registered client channel `{}`!",getName());
    }

    public void addDebugElements(MTDebugInfo info, Collection<Element> elements) {
        //if(this.playing) elements.add(new Element(CHANNEL,info.getTranslated("channel","song")));
    }

    @Override
    public boolean checkDeactivate(TriggerAPI current, TriggerAPI next) {
        if(Objects.nonNull(current)) {
            if(current.matches(next)) {
                this.deactivating = false;
                return false;
            }
            if(Objects.isNull(this.playingPool) || Objects.isNull(this.player.getPlayingTrack())) return true;
            this.deactivating = true;
            this.playingPool.queryInterrupt(next,this.player);
            return false;
        }
        return true;
    }

    @Override
    public void close() {
        super.close();
        this.listener.close();
        this.player.destroy();
        this.manager.shutdown();
        this.categoryVolume = 0f;
        this.trackVolume = 0f;
        this.queued = false;
        this.playingPool = null;
    }

    protected void configure(AudioConfiguration config) {
        config.setResamplingQuality(EnumHelper.getEnumOrDefault(getHelper().getDebugString("resampling_quality"),ResamplingQuality.class,HIGH));
        config.setOpusEncodingQuality(getHelper().getDebugNumber("encoding_quality").intValue());
        config.setOutputFormat(DISCORD_PCM_S16_BE);
    }

    protected AudioPlayerManager createManager() {
        AudioPlayerManager manager = new DefaultAudioPlayerManager();
        ChannelHelper.registerRemoteSources(this,manager);
        AudioSourceManagers.registerLocalSource(manager);
        return manager;
    }

    protected AudioPlayer createPlayer() {
        AudioPlayer player = this.manager.createPlayer();
        player.setVolume(0);
        return player;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.deactivating = false;
    }

    protected AudioConfiguration finalizeManager() {
        this.manager.setFrameBufferDuration(1000);
        this.manager.setPlayerCleanupThreshold(Long.MAX_VALUE);
        return this.manager.getConfiguration();
    }

    private @Nullable String findMatchingFile(String path) {
        String[] matches = getInfo().getLocalFolder().list((dir,name) -> name.equals(path) || name.startsWith(path+"."));
        return Objects.nonNull(matches) && matches.length>0 ? matches[0] : null;
    }

    @Override
    public AudioPlayer getPlayer() {
        return this.player;
    }
    
    @Nullable @Override public String getPlayingSongName() {
        if(Objects.isNull(this.playingPool)) return null;
        AudioRef ref = this.playingPool;
        while(ref instanceof AudioPool) ref = ((AudioPool)ref).getQueuedAudio();
        return Objects.nonNull(ref) ? ref.getName() : null;
    }
    
    @Nullable @Override public String getPlayingSongTime() {
        AudioTrack track = this.player.getPlayingTrack();
        if(Objects.isNull(track)) return null;
        String current = getFormattedTime(track.getPosition());
        String duration = getFormattedTime(track.getDuration());
        return current+"/"+duration;
    }
    
    protected String getFormattedTime(long millis) {
        String format = millis>=3600000 ? "HH:mm:ss:SSS" : (millis>=60000 ? "mm:ss:SSS" : "ss:SSS");
        return DurationFormatUtils.formatDuration(millis,format,false);
    }
    
    @Override protected String getTypeName() {
        return "ClientChannel";
    }
    
    @Override
    public boolean isClientChannel() {
        return true;
    }

    @Override
    public boolean isValid() {
        return Objects.nonNull(this.trackLoader);
    }

    @Override
    public void loadLocalTrack(AudioRef ref, String location) {
        if(getInfo().canReadFiles()) this.trackLoader.loadLocal(this.manager,ref,getInfo().getLocalFolder(),findMatchingFile(location));
        else logWarn("Unable to load track from file at `{}` for audio `{}` since the local folder does not exist!",
                location,ref.getName());
    }

    @Override
    public void loadRemoteTrack(AudioRef ref, String location) {
        this.trackLoader.loadRemote(this.manager,ref,location);
    }

    @Override
    public void onResourcesLoaded() {
        if(!this.registeredResourceAudio) {
            this.manager.registerSourceManager(new ResourceAudioSourceManager(this));
            this.registeredResourceAudio = true;
            logInfo("Successfully registered resource audio manager");
            logInfo("Finding resource tracks that need loading");
            getData().loadResourceTracks();
        }
    }

    @Override
    public void onTrackStart(AudioTrack track) {
    }

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {
        stopped();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void play() {
        super.play();
        this.queued = false;
        TriggerAPI trigger = getActiveTrigger();
        if(trigger.canPlayAudio()) {
            AudioPool pool = trigger.getAudioPool();
            pool.start(trigger);
            this.playingPool = pool;
        }
    }

    @Override
    public void playing() {
        super.playing();
    }

    @Override
    public void queue() {
        super.queue();
        this.queued = true;
    }

    @Override
    public void setCategoryVolume(float volume) {
        if(volume!=this.categoryVolume) {
            logDebug("Setting category volume to {}%",volume*100f);
            this.categoryVolume = volume;
            updateVolume();
        }
    }

    @Override
    public void setTrackVolume(float volume) {
        if(volume!=this.trackVolume) {
            logDebug("Setting track volume to {}%",volume*100f);
            this.trackVolume = volume;
            updateVolume();
        }
    }
    
    @Override public boolean shouldBlockMusicTicker() {
        return this.getInfo().isOverridesMusic() && Objects.nonNull(this.playingPool);
    }
    
    @Override
    public void stopped() {
        this.player.stopTrack();
        super.stopped();
        this.playingPool = null;
    }
    
    @Override
    public void tick() {
        if(MTClient.isUnpaused() && (MTClient.isFocused() || !getHelper().getDebugBool("pause_unless_focused"))) {
            this.player.setPaused(false);
            super.tick();
        } else this.player.setPaused(true);
    }

    @Override
    public void tickActive() {
        super.tickActive();
        TriggerAPI trigger = getActiveTrigger();
        if(Objects.nonNull(trigger)) {
            AudioPool activePool = getData().getActivePool();
            if(Objects.nonNull(activePool)) {
                if(Objects.nonNull(this.playingPool)) playing();
                else if(!this.deactivating) {
                    if(this.queued) play();
                    else queue();
                }
            }
        }
    }

    @Override
    public void tickSlow() {
        if(!this.trackLoader.isQueued()) super.tickSlow();
    }

    private void updateVolume() {
        this.player.setVolume((int)(100f*this.categoryVolume*this.trackVolume));
    }
}
