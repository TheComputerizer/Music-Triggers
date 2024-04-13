package mods.thecomputerizer.musictriggers.api.client.audio;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;

import javax.annotation.Nonnull;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine.Info;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("BusyWait")
public class AudioOutput extends Thread { //TODO Replace this

    protected final ChannelAPI channel;
    protected final AudioDataFormat format;
    protected boolean runAudioLoop;
    private Supplier<Void> onRun;

    public AudioOutput(@Nonnull ChannelAPI channel) {
        super("LavaPlayer Audio Thread");
        this.channel = channel;
        this.format = StandardAudioDataFormats.DISCORD_PCM_S16_BE;
        this.runAudioLoop = true;
        this.onRun = () -> {
            try {
                if(this.runAudioLoop) {
                    AudioInputStream stream = AudioPlayerInputStream.createStream(this.channel.getPlayer(),this.format,
                            this.format.frameDuration(),true);
                    Info info = new DataLine.Info(SourceDataLine.class,stream.getFormat());
                    SourceDataLine output = (SourceDataLine)AudioSystem.getLine(info);
                    int buffersize = this.format.chunkSampleCount*this.format.channelCount*2;
                    output.open(stream.getFormat(),buffersize*5);
                    output.start();
                    byte[] buffer = new byte[buffersize];
                    int chunkSize;
                    long frameDuration = this.format.frameDuration();
                    while(this.runAudioLoop) {
                        if(!this.channel.getPlayer().isPaused()) {
                            if((chunkSize = stream.read(buffer))>=0) output.write(buffer,0,chunkSize);
                            else throw new IllegalStateException("Audiostream ended for channel "+
                                    this.channel.getName()+"! This should not happen.");
                        } else {
                            output.drain();
                            sleep(frameDuration);
                        }
                    }
                } else {
                    sleep(250);
                    this.runAudioLoop = true;
                }
            } catch(Exception ex) {
                this.channel.logError("An unkown error occured in the audio output thread!",ex);
            }
            return null;
        };
    }

    /**
     * Note: I'm not sure what the best way of "closing" a thread that isn't needed anymore is or if I can just ignore
     * it but this basically does the same thing.
     */
    public void close() {
        this.onRun = null;
    }

    public void setPitch(float pitch) {

    }

    public void pauseAudioLoop() {
        this.runAudioLoop = false;
    }

    @Override
    public void run() {
        if(Objects.nonNull(this.onRun)) this.onRun.get();
    }
}