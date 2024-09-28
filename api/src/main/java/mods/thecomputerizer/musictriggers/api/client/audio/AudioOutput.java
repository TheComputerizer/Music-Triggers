package mods.thecomputerizer.musictriggers.api.client.audio;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import mods.thecomputerizer.musictriggers.api.client.MTClientEvents;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;

import javax.annotation.Nonnull;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine.Info;
import java.util.Objects;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_PCM_S16_BE;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;

public class AudioOutput extends Thread { //TODO Replace this

    protected final ChannelAPI channel;
    protected boolean runAudioLoop;
    private Runnable onRun;

    public AudioOutput(@Nonnull ChannelAPI channel) {
        super(NAME+" Audio Output ["+channel.getName()+"]");
        this.channel = channel;
        this.runAudioLoop = true;
        this.onRun = () -> {
            AudioDataFormat format = DISCORD_PCM_S16_BE;
            if(this.runAudioLoop) loopRunner(this.channel.getPlayer(),format,format.frameDuration());
            else {
                try {
                    sleep(250);
                    this.runAudioLoop = true;
                } catch(InterruptedException ex) {
                    this.channel.logFatal("Unable to restart audio output!",ex);
                    this.onRun = null;
                }
            }
        };
    }

    /**
     * Note: I'm not sure what the best way of "closing" a thread that isn't necessary anymore is or if I can just
     * ignore it, but this basically does the same thing.
     */
    public void close() {
        this.onRun = null;
    }

    private void loopRunner(AudioPlayer player, AudioDataFormat format, long frameDur) {
        try(AudioInputStream stream = AudioPlayerInputStream.createStream(player,format,frameDur,true)) {
            Info info = new DataLine.Info(SourceDataLine.class,stream.getFormat());
            SourceDataLine output = (SourceDataLine)AudioSystem.getLine(info);
            int buffersize = format.chunkSampleCount*format.channelCount*2;
            output.open(stream.getFormat(),buffersize*5);
            output.start();
            byte[] buffer = new byte[buffersize];
            int chunkSize;
            while(this.runAudioLoop) {
                if(!player.isPaused()) {
                    if((chunkSize = stream.read(buffer))>=0) output.write(buffer,0,chunkSize);
                    else {
                        String msg = "Audiostream ended for channel "+this.channel+"! This should not happen!";
                        throw new IllegalStateException(msg);
                    }
                } else {
                    output.drain();
                    //noinspection BusyWait
                    sleep(frameDur);
                }
            }
        } catch(Exception ex) {
            this.channel.logError("An unkown error occured in the audio output thread!",ex);
        }
    }

    public void pauseAudioLoop() {
        this.runAudioLoop = false;
        MTClientEvents.handleError(TILRef.getClientSubAPI(ClientAPI::getMinecraft),this.channel.getName());
    }

    @Override public void run() {
        if(Objects.nonNull(this.onRun)) this.onRun.run();
    }
    
    public void unpauseAudioLoop() {
        this.runAudioLoop = true;
    }
}