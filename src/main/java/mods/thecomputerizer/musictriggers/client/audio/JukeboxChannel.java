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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.apache.logging.log4j.Level;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class JukeboxChannel {
    private static final AudioDataFormat FORMAT = new Pcm16AudioDataFormat(2, 48000, 960, true);
    private final AudioPlayer player;
    private BlockPos pos;

    public JukeboxChannel(String channel) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        this.player = playerManager.createPlayer();
        this.player.setVolume(100);
        new ChannelListener(this.player, FORMAT, null);
        playerManager.setFrameBufferDuration(1000);
        playerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);
        playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        playerManager.getConfiguration().setOutputFormat(FORMAT);
        MusicTriggers.logExternally(Level.INFO,"Registered jukebox channel "+channel);
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
        } else {
            MusicTriggers.logExternally(Level.ERROR,"Could not play disc!");
        }
    }

    public void stopTrack() {
        this.getPlayer().stopTrack();
    }
}
