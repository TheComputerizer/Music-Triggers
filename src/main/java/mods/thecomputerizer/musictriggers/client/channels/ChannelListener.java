package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.core.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.Objects;

@SideOnly(value = Side.CLIENT)
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
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException ex) {
        String msg = "Track exception caught! Restarting audio output for channel {}";
        MusicTriggers.logExternally(Level.ERROR,msg,this.channel.getChannelName());
        Constants.MAIN_LOG.error(msg,this.channel.getChannelName(),ex);
        this.audioOutputThread.pauseAudioLoop();
        this.channel.onTrackStop(AudioTrackEndReason.LOAD_FAILED);
    }

    public void setPitch(float pitch) {
        this.audioOutputThread.setPitch(pitch);
    }
}
