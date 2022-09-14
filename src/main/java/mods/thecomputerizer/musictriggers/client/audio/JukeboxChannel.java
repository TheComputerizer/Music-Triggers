package mods.thecomputerizer.musictriggers.client.audio;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.Pcm16AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Objects;

public class JukeboxChannel {
    private static final AudioDataFormat FORMAT = new Pcm16AudioDataFormat(2, 48000, 960, true);
    private final String channel;
    private final SoundCategory category;
    private final AudioPlayerManager playerManager;
    private AudioPlayer player;
    private ChannelListener listener;
    private final HashMap<String, AudioTrack> loadedTracks;
    private BlockPos pos;

    public JukeboxChannel(String channel) {
        this.channel = channel;
        this.category = SoundCategory.RECORDS;
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
        this.player = refreshPlayer();
        this.loadedTracks = new HashMap<>();
        this.playerManager.setFrameBufferDuration(1000);
        this.playerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        this.playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        this.playerManager.getConfiguration().setOutputFormat(FORMAT);
        MusicTriggers.logger.info("Registered jukebox channel "+channel);
    }

    private AudioPlayer refreshPlayer() {
        if(this.player!=null) {
            this.player.destroy();
            this.listener.stopThread();
        }
        AudioPlayer newPlayer = playerManager.createPlayer();
        newPlayer.setVolume(100);
        this.listener = new ChannelListener(newPlayer, FORMAT, this.channel);
        return newPlayer;
    }

    public AudioPlayer getPlayer() {
        return this.player;
    }

    public AudioTrack getCurPlaying() {
        return this.player.getPlayingTrack();
    }

    public void checkStopPlaying(boolean reloading) {
        if(isPlaying()) {
            if(isPlaying())  {
                if(reloading) stopTrack();
                else if(this.pos!=null && MinecraftClient.getInstance().world!=null && MinecraftClient.getInstance().world.getChunk(this.pos).getBlockEntity(this.pos) instanceof JukeboxBlockEntity && !Objects.requireNonNull(MinecraftClient.getInstance().world.getChunk(this.pos).getBlockEntity(this.pos)).getCachedState().get(JukeboxBlock.HAS_RECORD))
                    stopTrack();
            }
        }
    }

    public boolean isPlaying() {
        return getCurPlaying()!=null;
    }

    public void setVolume(float volume) {
        this.getPlayer().setVolume((int)(volume*getChannelVolume()*100));
    }

    private float getChannelVolume() {
        float master = MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER);
        return master*MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.valueOf(this.channel));
    }

    public void setAttenuation() {

    }

    public void playTrack(AudioTrack track, BlockPos jukeboxPos) {
        MusicTriggers.logger.info("Playing track for jukebox");
        if(track!=null) {
            track.setPosition(0);
            try {
                if (!this.getPlayer().startTrack(track, false)) MusicTriggers.logger.error("Could not start track!");
                else this.pos = jukeboxPos;
            } catch (IllegalStateException e) {
                if (!this.getPlayer().startTrack(track.makeClone(), false)) MusicTriggers.logger.error("Could not start track!");
            }
        } else {
            MusicTriggers.logger.error("Could not play disc!");
        }
    }

    public void stopTrack() {
        this.getPlayer().stopTrack();
    }
}
