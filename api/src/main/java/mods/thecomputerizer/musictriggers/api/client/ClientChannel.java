package mods.thecomputerizer.musictriggers.api.client;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelListener;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import org.apache.commons.lang3.EnumUtils;

import java.util.Objects;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_PCM_S16_BE;
import static com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality.HIGH;

public class ClientChannel extends ChannelAPI {

    private final AudioPlayerManager manager;
    private final AudioPlayer player;
    private final ChannelListener listener;

    public ClientChannel(Table table) {
        super(table);
        this.manager = createManager();
        this.player = createPlayer();
        configure(finalizeManager());
        this.listener = new ChannelListener(this);
        logInfo("Successfully registered client channel `{}`!",getName());
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

    @Override
    public AudioPlayer getPlayer() {
        return this.player;
    }

    @Override
    protected TriggerSelectorAPI<?, ?> getSelector() {
        return null;
    }

    @Override
    public boolean isClientChannel() {
        return true;
    }

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {

    }

    @Override
    public void tickFast() {
    }

    @Override
    public void tickSlow() {
    }

    @Override
    public void playable() {

    }
}
