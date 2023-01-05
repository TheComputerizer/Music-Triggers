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
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class JukeboxChannel {
    private static final AudioDataFormat FORMAT = new Pcm16AudioDataFormat(2, 48000, 960, true);
    private final String channel;
    private final SoundSource category;
    private final AudioPlayerManager playerManager;
    private AudioPlayer player;
    private ChannelListener listener;
    private final HashMap<String, AudioTrack> loadedTracks;
    private BlockPos pos;

    public JukeboxChannel(String channel) {
        this.channel = channel;
        this.category = SoundSource.RECORDS;
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
        MusicTriggers.logExternally(Level.INFO,"Registered jukebox channel "+channel);
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
                else if(this.pos!=null && Minecraft.getInstance().level!=null &&
                        Minecraft.getInstance().level.getChunk(this.pos).getBlockEntity(this.pos) instanceof JukeboxBlockEntity &&
                        !Objects.requireNonNull(Minecraft.getInstance().level.getChunk(this.pos).getBlockEntity(this.pos)).getBlockState().getValue(JukeboxBlock.HAS_RECORD))
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
        float master = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        return master*Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.valueOf(this.channel));
    }

    public void setAttenuation() {

    }

    public void playTrack(AudioTrack track, BlockPos jukeboxPos) {
        MusicTriggers.logExternally(Level.INFO,"Playing track for jukebox");
        if(track!=null) {
            track.setPosition(0);
            try {
                if (!this.getPlayer().startTrack(track, false)) MusicTriggers.logExternally(Level.ERROR,"Could not start track!");
                else this.pos = jukeboxPos;
            } catch (IllegalStateException e) {
                if (!this.getPlayer().startTrack(track.makeClone(), false)) MusicTriggers.logExternally(Level.ERROR,"Could not start track!");
            }
        } else MusicTriggers.logExternally(Level.ERROR,"Could not play disc!");
    }

    public void stopTrack() {
        this.getPlayer().stopTrack();
    }

    public void reload() {

    }
}