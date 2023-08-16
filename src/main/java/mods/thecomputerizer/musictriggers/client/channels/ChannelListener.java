package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Level;

import java.util.Objects;

@OnlyIn(value = Dist.CLIENT)
public class ChannelListener extends AudioEventAdapter {

    private static final boolean OPENAL_TEST = false;

    private final AudioOutput audioOutputThread;
    private final IChannel channel;

    public ChannelListener(IChannel channel) {
        if(Objects.isNull(channel))
            throw new RuntimeException("Cannot add listener to null audio channel!");
        this.channel = channel;
        this.audioOutputThread = OPENAL_TEST ? new OpenALTest(channel) : new AudioOutput(channel);
        channel.getPlayer().addListener(this);
        this.audioOutputThread.start();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.channel.onTrackStop(endReason);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        MusicTriggers.logExternally(Level.ERROR, "Track exception caught! Restarting audio output for channel" +
                " {}",this.channel.getChannelName());
        Constants.MAIN_LOG.error("Track exception caught! Restarting audio output for channel" +
                " {}",this.channel.getChannelName(),exception);
        this.audioOutputThread.pauseAudioLoop();
        this.channel.onTrackStop(AudioTrackEndReason.LOAD_FAILED);
    }

    public void setPitch(float pitch) {
        this.audioOutputThread.setPitch(pitch);
    }
}
