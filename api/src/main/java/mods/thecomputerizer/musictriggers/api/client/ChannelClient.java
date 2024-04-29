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
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.api.util.EnumHelper;

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

    public ChannelClient(ChannelHelper helper, Table table) {
        super(helper,table);
        this.manager = createManager();
        this.player = createPlayer();
        configure(finalizeManager());
        this.listener = new ChannelListener(this);
        this.trackLoader = new TrackLoader();
        logInfo("Successfully registered client channel `{}`!",getName());
    }

    @Override
    public void activate() { //TODO Remove hardcoded testing stuff
        super.activate();
        handleInterruption(getActiveTrigger());
    }

    public void addDebugElements(MTDebugInfo info, Collection<Element> elements) {
        //if(this.playing) elements.add(new Element(CHANNEL,info.getTranslated("channel","song")));
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
        config.setResamplingQuality(EnumHelper.getEnumOrDefault("RESAMPLING_QUALITY",ResamplingQuality.class,HIGH));
        config.setOpusEncodingQuality(getHelper().getDebugNumber("ENCODING_QUALITY").intValue());
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

    protected void handleInterruption(@Nullable TriggerAPI trigger) {
        logDebug("Handling interruption");
        if(Objects.isNull(this.playingPool) || this.playingPool.isInterrputedBy(trigger)) stop();
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
        this.categoryVolume = volume;
        updateVolume();
    }

    @Override
    public void setTrackVolume(float volume) {
        this.trackVolume = volume;
        updateVolume();
    }

    @Override
    public void stop() {
        super.stop();
        if(Objects.nonNull(this.playingPool)) this.playingPool.stop();
    }

    @Override
    public void stopped() {
        super.stopped();
        this.playingPool = null;
    }

    @Override
    public void tick() {
        super.tick();
        if(Objects.nonNull(this.player.getPlayingTrack())) playing();
    }

    @Override
    public void tickActive() {
        super.tickActive();
        TriggerAPI trigger = getActiveTrigger();
        if(Objects.nonNull(trigger)) {
            AudioPool activePool = getData().getActivePool();
            if(Objects.nonNull(activePool)) {
                if(Objects.nonNull(this.playingPool)) playing();
                else if(this.queued) play();
                else queue();
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
