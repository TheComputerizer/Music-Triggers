package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@OnlyIn(value = Dist.CLIENT)
public class JukeboxChannel implements IChannel {
    private final AudioPlayer player;
    private BlockPos pos;
    private float masterVol = 1f;
    private float recordsVol = 1f;
    private boolean needsVolumeUpdate = false;

    public JukeboxChannel() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        ChannelManager.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        this.player = playerManager.createPlayer();
        this.player.setVolume(100);
        new ChannelListener(this);
        playerManager.setFrameBufferDuration(1000);
        playerManager.setPlayerCleanupThreshold(Long.MAX_VALUE);
        String resamplingQuality = ConfigDebug.RESAMPLING_QUALITY.toUpperCase();
        playerManager.getConfiguration().setResamplingQuality(EnumUtils.isValidEnum(
                AudioConfiguration.ResamplingQuality.class,resamplingQuality) ?
                AudioConfiguration.ResamplingQuality.valueOf(resamplingQuality) :
                AudioConfiguration.ResamplingQuality.HIGH);
        playerManager.getConfiguration().setOpusEncodingQuality(ConfigDebug.ENCODING_QUALITY);
        playerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);
        MusicTriggers.logExternally(Level.INFO,"Registered jukebox channel");
    }

    @Override
    public String getChannelName() {
        return "jukebox";
    }

    @Override
    public AudioPlayer getPlayer() {
        return this.player;
    }

    @Override
    public void tickFast() {
        if(this.needsVolumeUpdate) setVolume(Minecraft.getInstance().player);
    }

    private void setVolume(@Nullable LocalPlayer player) {
        if(Objects.nonNull(player)) {
            float distFactor = Objects.nonNull(this.pos) ? getDistFactor(player,this.pos) : 0f;
            this.player.setVolume((int)(this.masterVol*this.recordsVol*distFactor*100f));
            this.needsVolumeUpdate = false;
        }
    }

    private float getDistFactor(@Nonnull LocalPlayer player, @Nonnull BlockPos pos) {
        return Math.max(0f,1f-((float)Math.sqrt(player.distanceToSqr(pos.getX(),pos.getY(),pos.getZ()))/63f));
    }

    @Override
    public void onSetSound(SoundSource category, float volume) {
        if(category==SoundSource.MASTER) {
            this.masterVol = volume;
            this.needsVolumeUpdate = true;
        }
        else if(category==SoundSource.RECORDS) {
            this.recordsVol = volume;
            this.needsVolumeUpdate = true;
        }
    }

    public AudioTrack getCurPlaying() {
        return this.player.getPlayingTrack();
    }

    public void checkStopPlaying(boolean reloading) {
        if(isPlaying()) {
            if(isPlaying())  {
                if(reloading) stopTrack();
                else if(Objects.nonNull(this.pos) && Objects.nonNull(Minecraft.getInstance().level)) {
                    BlockEntity tile = Minecraft.getInstance().level.getChunk(this.pos).getBlockEntity(this.pos);
                    if(tile instanceof JukeboxBlockEntity && !tile.getBlockState().getValue(JukeboxBlock.HAS_RECORD))
                        stopTrack();
                }
            }
        }
    }

    public boolean isPlaying() {
        return Objects.nonNull(getCurPlaying());
    }

    public void playTrack(AudioTrack track, BlockPos jukeboxPos) {
        MusicTriggers.logExternally(Level.INFO,"Playing track for jukebox");
        if(Objects.nonNull(track)) {
            track.setPosition(0);
            try {
                if(!this.getPlayer().startTrack(track,false))
                    MusicTriggers.logExternally(Level.ERROR,"Could not start track!");
                else this.pos = jukeboxPos;
            } catch(IllegalStateException e) {
                if(!this.getPlayer().startTrack(track.makeClone(), false))
                    MusicTriggers.logExternally(Level.ERROR,"Could not start track!");
            }
        } else
            MusicTriggers.logExternally(Level.ERROR,"Could not play disc!");
    }

    public void stopTrack() {
        this.getPlayer().stopTrack();
    }

    @Override
    public void onTrackStop(AudioTrackEndReason endReason) {}

    @Override
    public void initCache() {}
}