package mods.thecomputerizer.musictriggers.client.channels;

import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import org.apache.logging.log4j.Level;
import paulscode.sound.SoundSystem;

import javax.annotation.Nonnull;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * Test class for attempting OpenAL (paulscode) integration
 */
@SuppressWarnings("BusyWait")
public class OpenALTest extends AudioOutput {

    private SoundSystem system;
    private boolean initialized;

    public OpenALTest(@Nonnull IChannel channel) {
        super(channel);
        this.runAudioLoop = initialized = false;
    }

    @Override
    public void pauseAudioLoop() {
        this.runAudioLoop = false;
        this.initialized = false;
    }

    @Override
    public void setPitch(float pitch) {
        if(this.initialized) this.system.setPitch(this.channel.getChannelName(),pitch);
    }

    private void init(AudioFormat format) {
        this.system = Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem;
        this.system.rawDataStream(format,true,this.channel.getChannelName(),1f,1f,1f,
                ISound.AttenuationType.NONE.getTypeInt(),1f);
        MusicTriggers.logExternally(Level.ERROR,"INITIALIZED CHANNEL LISTENER FOR CHANNEL {}",
                this.channel.getChannelName());
        this.initialized = true;
    }

    @Override
    public void run() {
        try {
            if(this.runAudioLoop) {
                AudioInputStream stream = AudioPlayerInputStream.createStream(this.channel.getPlayer(),
                        this.format, this.format.frameDuration(), true);
                if(!this.initialized) init(stream.getFormat());
                int buffersize = this.format.chunkSampleCount * this.format.channelCount * 2;
                byte[] buffer = new byte[buffersize];
                long frameDuration = this.format.frameDuration();
                while (this.runAudioLoop) {
                    if (!this.channel.getPlayer().isPaused()) {
                        if (stream.read(buffer) >= 0)
                            this.system.feedRawAudioData(this.channel.getChannelName(),buffer);
                        else throw new IllegalStateException("Audiostream ended. This should not happen.");
                    } else sleep(frameDuration);
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
