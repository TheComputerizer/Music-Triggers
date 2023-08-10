package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import mods.thecomputerizer.musictriggers.Constants;

import javax.annotation.Nonnull;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

@SuppressWarnings("BusyWait")
class AudioOutput extends Thread {

    protected final IChannel channel;
    protected final AudioDataFormat format;
    protected boolean runAudioLoop;

    public AudioOutput(@Nonnull IChannel channel) {
        super("LavaPlayer Audio Thread");
        this.channel = channel;
        this.format = StandardAudioDataFormats.DISCORD_PCM_S16_BE;
        this.runAudioLoop = true;
    }

    protected void setPitch(float pitch) {

    }

    public void pauseAudioLoop() {
        this.runAudioLoop = false;
    }

    @Override
    public void run() {
        try {
            if (this.runAudioLoop) {
                AudioInputStream stream = AudioPlayerInputStream.createStream(this.channel.getPlayer(), this.format,
                        this.format.frameDuration(), true);
                SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat());
                SourceDataLine output = (SourceDataLine) AudioSystem.getLine(info);
                int buffersize = this.format.chunkSampleCount * this.format.channelCount * 2;
                output.open(stream.getFormat(), buffersize * 5);
                output.start();
                byte[] buffer = new byte[buffersize];
                int chunkSize;
                long frameDuration = this.format.frameDuration();
                while (this.runAudioLoop) {
                    if (!this.channel.getPlayer().isPaused()) {
                        if ((chunkSize = stream.read(buffer)) >= 0) output.write(buffer, 0, chunkSize);
                        else throw new IllegalStateException("Audiostream ended for channel "+
                                this.channel.getChannelName()+". This should not happen.");
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
            Constants.MAIN_LOG.error("An unkown error occured in the audio output thread for channel {}!",
                    this.channel.getChannelName(),ex);
        }
    }
}
