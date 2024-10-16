package mods.thecomputerizer.musictriggers.api.client.channel;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.client.MTClient;
import mods.thecomputerizer.musictriggers.api.client.MTClientEvents;
import mods.thecomputerizer.musictriggers.api.client.audio.TrackLoader;
import mods.thecomputerizer.musictriggers.api.client.audio.resource.ResourceAudioSourceManager;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelListener;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.Link;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.EnumHelper;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_PCM_S16_BE;
import static com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality.HIGH;
import static java.lang.Long.MAX_VALUE;

public class ChannelClient extends ChannelAPI {

    private final AudioPlayerManager manager;
    protected final AudioPlayer player;
    private final ChannelListener listener;
    private final TrackLoader trackLoader;
    private boolean registeredResourceAudio;
    private float categoryVolume;
    private float masterVolume;
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
        this.trackLoader = new TrackLoader(this);
    }

    @Override public boolean checkDeactivate(TriggerAPI current, TriggerAPI next) {
        if(Objects.nonNull(current)) {
            if(current.matches(next)) {
                this.deactivating = false;
                return false;
            }
            if(Objects.isNull(this.playingPool) ||
               (Objects.isNull(this.player.getPlayingTrack()) && !this.playingPool.isQueued())) return true;
            if(!this.deactivating) deactivateLink();
            this.deactivating = true;
            this.playingPool.queryInterrupt(next,this.player);
            return false;
        }
        return true;
    }
    
    public boolean checkFocus() {
        return MTClient.isFocused() || !ChannelHelper.getDebugBool("pause_unless_focused");
    }
    
    public boolean checkJukebox(boolean jukebox) {
        return !jukebox || !getInfo().isPausedByJukebox();
    }
    
    public boolean checkPaused(boolean unpaused) {
        return unpaused || getInfo().hasPausedMusic();
    }

    @Override public void close() {
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
        String resamplingQuality = ChannelHelper.getDebugString("resampling_quality");
        config.setResamplingQuality(EnumHelper.getEnumOrDefault(resamplingQuality,ResamplingQuality.class,HIGH));
        config.setOpusEncodingQuality(ChannelHelper.getDebugNumber("encoding_quality").intValue());
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

    @Override public void deactivate() {
        super.deactivate();
        this.deactivating = false;
    }
    
    @Override public void disable(Link link) {
        super.disable(link);
        if(Objects.nonNull(this.playingPool)) stop();
    }

    protected AudioConfiguration finalizeManager() {
        this.manager.setFrameBufferDuration(1000);
        this.manager.setPlayerCleanupThreshold(MAX_VALUE);
        return this.manager.getConfiguration();
    }

    private @Nullable String findMatchingFile(String path) {
        String[] matches = getInfo().getLocalFolder().list((dir,name) -> name.equals(path) || name.startsWith(path+"."));
        return Objects.nonNull(matches) && matches.length>0 ? matches[0] : null;
    }
    
    @Nullable @Override public String getFormattedSongTime() {
        AudioTrack track = this.player.getPlayingTrack();
        if(Objects.isNull(track)) return null;
        String current = getFormattedTime(getPlayingSongTime());
        String duration = getFormattedTime(track.getDuration());
        return current+"/"+duration;
    }
    
    protected String getFormattedTime(long millis) {
        String format = millis>=3600000 ? "HH:mm:ss:SSS" : (millis>=60000 ? "mm:ss:SSS" : "ss:SSS");
        return DurationFormatUtils.formatDuration(millis,format);
    }
    
    @Override public String getLogType() {
        return "CLIENT";
    }

    @Override public AudioPlayer getPlayer() {
        return this.player;
    }
    
    @Nullable @Override public AudioPool getPlayingPool() {
        return this.playingPool;
    }
    
    @Nullable @Override public String getPlayingSongName() {
        if(Objects.isNull(this.playingPool)) return null;
        AudioRef ref = this.playingPool;
        while(ref instanceof AudioPool) ref = ((AudioPool)ref).getQueuedAudio();
        return Objects.nonNull(ref) ? ref.getName() : null;
    }
    
    @Override public long getPlayingSongTime() {
        AudioTrack track = this.player.getPlayingTrack();
        double speed = Objects.nonNull(this.playingPool) ? this.playingPool.getSpeed() : 1d;
        return Objects.nonNull(track) ? (long)(track.getPosition()*speed) : 0L;
    }
    
    @Override public boolean isClientChannel() {
        return true;
    }

    @Override public boolean isValid() {
        return Objects.nonNull(this.trackLoader);
    }

    @Override public String loadLocalTrack(AudioRef ref, String location) {
        if(getInfo().canReadFiles()) {
            File folder = getInfo().getLocalFolder();
            String match = findMatchingFile(location);
            this.trackLoader.loadLocal(this.manager,ref,folder,match);
            return Objects.nonNull(match) ? match : location;
        }
        logWarn("Unable to load track from file at `{}` for audio `{}` since the local folder does not exist!",
                location,ref.getName());
        return location;
    }

    @Override public String loadRemoteTrack(AudioRef ref, String location) {
        this.trackLoader.loadRemote(this.manager,ref,location);
        return location;
    }
    
    @Override public void onResourcesLoaded() {
        if(!this.registeredResourceAudio) {
            this.manager.registerSourceManager(new ResourceAudioSourceManager(this));
            this.registeredResourceAudio = true;
            logInfo("Successfully registered resource audio manager");
            logInfo("Finding resource tracks that need loading");
            getData().loadResourceTracks();
        }
    }

    @Override public void onTrackStart(AudioTrack track) {}

    @Override public void onTrackStop(AudioTrackEndReason endReason) {
        stopped();
    }

    @Override public void play(boolean unpaused) {
        super.play(unpaused);
        this.queued = false;
        TriggerAPI trigger = getActiveTrigger();
        if(trigger.canPlayAudio()) {
            AudioPool pool = trigger.getAudioPool();
            if(Objects.nonNull(pool)) {
                if(pool.hasQueue()) {
                    pool.start(trigger,unpaused);
                    this.playingPool = pool;
                } else pool.queue();
            }
        }
    }

    @Override public void playing(boolean unpaused) {
        super.playing(unpaused);
    }

    @Override public void queue() {
        super.queue();
        this.queued = true;
    }

    @Override public void setCategoryVolume(float volume) {
        if(volume!=this.categoryVolume) {
            logDebug("Setting category volume to {}%",volume*100f);
            this.categoryVolume = volume;
            updateVolume();
        }
    }
    
    @Override public void setMasterVolume(float volume) {
        if(volume!=this.masterVolume) {
            logDebug("Setting master volume to {}%",volume*100f);
            this.masterVolume = volume;
            updateVolume();
        }
    }
    
    @Override public void setTrackVolume(float volume) {
        if(volume!=this.trackVolume) {
            this.trackVolume = volume;
            updateVolume();
        }
    }
    
    @Override public boolean showDebugSongInfo() {
        return super.showDebugSongInfo() && Objects.nonNull(this.player.getPlayingTrack());
    }
    
    @Override public boolean shouldBlockMusicTicker() {
        return this.getInfo().isOverridesMusic() || Objects.nonNull(this.playingPool);
    }
    
    @Override public void stopped() {
        this.player.stopCurrentTrack();
        super.stopped();
        this.playingPool = null;
    }
    
    @Override public boolean tick(boolean jukebox, boolean unpaused) {
        if(this.listener.isBroken()) {
            MTClientEvents.handleError(ClientHelper.getMinecraft(),getName());
            return false;
        }
        if(this.enabled) {
            unpaused = MTClient.isUnpaused();
            if(checkPaused(unpaused) && checkFocus() && checkJukebox(jukebox)) {
                this.player.setPaused(false);
                unpaused = super.tick(jukebox,unpaused);
            } else this.player.setPaused(true);
            return unpaused || !getInfo().hasPausedMusic();
        } else if(Objects.nonNull(this.playingPool)) playing(unpaused);
        return false;
    }

    @Override public void tickActive(boolean unpaused) {
        super.tickActive(unpaused);
        TriggerAPI trigger = getActiveTrigger();
        if(Objects.nonNull(trigger)) {
            AudioPool activePool = getData().getActivePool();
            if(Objects.nonNull(activePool)) {
                if(Objects.nonNull(this.playingPool)) playing(unpaused);
                else if(!this.deactivating) {
                    if(this.queued) play(unpaused);
                    else queue();
                }
            }
        }
    }
    
    @Override public void tickSlow(boolean unpaused) {
        if(checkPaused(unpaused)) super.tickSlow(unpaused);
    }

    private void updateVolume() {
        this.player.setVolume((int)(100f*this.masterVolume*this.categoryVolume*this.trackVolume));
    }
}