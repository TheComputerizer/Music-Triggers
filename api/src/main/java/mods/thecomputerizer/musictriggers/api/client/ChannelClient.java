package mods.thecomputerizer.musictriggers.api.client;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.client.audio.TrackLoader;
import mods.thecomputerizer.musictriggers.api.client.audio.resource.ResourceAudioSourceManager;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelListener;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import org.apache.commons.lang3.EnumUtils;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_PCM_S16_BE;
import static com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality.HIGH;

public class ChannelClient extends ChannelAPI {

    private final AudioPlayerManager manager;
    private final AudioPlayer player;
    private final ChannelListener listener;
    private final TrackLoader trackLoader;
    private boolean registeredResourceAudio;
    private float categoryVolume;
    private float trackVolume;

    public ChannelClient(Table table) {
        super(table);
        this.manager = createManager();
        this.player = createPlayer();
        configure(finalizeManager());
        this.listener = new ChannelListener(this);
        this.trackLoader = new TrackLoader();
        logInfo("Successfully registered client channel `{}`!",getName());
    }

    @Override
    public void activate() {
        super.activate();
        queue();
    }

    protected void configure(AudioConfiguration config) {
        ResamplingQuality quality = EnumUtils.getEnum(ResamplingQuality.class,
                ChannelHelper.getDebugString("RESAMPLING_QUALITY"));
        config.setResamplingQuality(Objects.nonNull(quality) ? quality : HIGH);
        config.setOpusEncodingQuality(ChannelHelper.getDebugNumber("ENCODING_QUALITY").intValue());
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
        if(getInfo().canReadFiles()) this.trackLoader.loadLocal(this.manager,ref,findMatchingFile(location));
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
        }
    }

    @Override
    public void onTrackStart(AudioTrack track) {
        play();
    }

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {
        stopped();
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
    public void tick() {
        super.tick();
        if(Objects.nonNull(this.player.getPlayingTrack())) playing();
    }

    private void updateVolume() {
        this.player.setVolume((int)(100f*this.categoryVolume*this.trackVolume));
    }
}
