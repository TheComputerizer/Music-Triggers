package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.util.Objects;

@SideOnly(value = Side.CLIENT)
public class ChannelListener extends AudioEventAdapter {
    private final AudioPlayer audioPlayer;
    private final AudioDataFormat format;

    private final AudioOutput AUDIO_THREAD;
    private final Channel channel;

    public ChannelListener(AudioPlayer audioPlayer, AudioDataFormat format, Channel channel) {
        this.audioPlayer = audioPlayer;
        this.format = format;
        this.channel = channel;
        this.AUDIO_THREAD = new AudioOutput(true);
        this.audioPlayer.addListener(this);
        this.AUDIO_THREAD.start();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if(Objects.nonNull(this.channel))
            this.channel.onTrackStart();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(Objects.nonNull(this.channel))
            this.channel.onTrackStop(endReason);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        MusicTriggers.logExternally(Level.ERROR,"Track exception caught! Restarting audio output for channel {}",this.channel);
        exception.printStackTrace();
        this.AUDIO_THREAD.setRunAudioLoop(false);
        if(Objects.nonNull(this.channel))
            this.channel.onTrackStop(AudioTrackEndReason.LOAD_FAILED);
    }

    @SuppressWarnings("BusyWait")
    private class AudioOutput extends Thread {

        private boolean runAudioLoop;

        public AudioOutput(boolean runAudioLoop) {
            super("LavaPlayer Audio Thread");
            this.runAudioLoop = runAudioLoop;
        }

        public void setRunAudioLoop(boolean shouldPlay) {
            this.runAudioLoop = shouldPlay;
        }

        @Override
        public void run() {
            try {
                if(this.runAudioLoop) {
                    AudioInputStream stream = AudioPlayerInputStream.createStream(ChannelListener.this.audioPlayer, ChannelListener.this.format, ChannelListener.this.format.frameDuration(), true);
                    SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat());
                    SourceDataLine output = (SourceDataLine) AudioSystem.getLine(info);
                    int buffersize = ChannelListener.this.format.chunkSampleCount * ChannelListener.this.format.channelCount * 2;
                    output.open(stream.getFormat(), buffersize * 5);
                    output.start();
                    byte[] buffer = new byte[buffersize];
                    int chunkSize;
                    long frameDuration = ChannelListener.this.format.frameDuration();
                    while (this.runAudioLoop) {
                        if (!ChannelListener.this.audioPlayer.isPaused()) {
                            if ((chunkSize = stream.read(buffer)) >= 0) output.write(buffer, 0, chunkSize);
                            else throw new IllegalStateException("Audiostream ended. This should not happen.");
                        } else {
                            output.drain();
                            sleep(frameDuration);
                        }
                    }
                } else {
                    sleep(250);
                    this.runAudioLoop = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
