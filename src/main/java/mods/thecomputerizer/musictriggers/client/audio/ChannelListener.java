package mods.thecomputerizer.musictriggers.client.audio;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.openal.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.OptionalInt;

@SuppressWarnings("deprecation")
@OnlyIn(value = Dist.CLIENT)
public class ChannelListener extends AudioEventAdapter {
    private static final int OPEN_AL_FORMAT = 4355;
    private final AudioPlayer audioPlayer;
    private final AudioDataFormat format;

    private final AudioOutput AUDIO_THREAD;
    private final String channel;

    public ChannelListener(AudioPlayer audioPlayer, AudioDataFormat format, String channel) {
        this.audioPlayer = audioPlayer;
        this.format = format;
        this.channel = channel;
        this.AUDIO_THREAD = new AudioOutput(true);
        this.audioPlayer.addListener(this);
        this.AUDIO_THREAD.start();
        MusicTriggers.logger.info("started thread");
    }

    public void stopThread() {
        this.AUDIO_THREAD.stop();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {

    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        MusicTriggers.logger.error("Track exception caught! Restarting audio output for channel: "+this.channel);
        exception.printStackTrace();
        this.AUDIO_THREAD.setRunAudioLoop(false);
    }

    @SuppressWarnings({"BusyWait", "ConstantConditions"})
    private class AudioOutput extends Thread {

        private boolean runAudioLoop;
        private int openALSource = 0;
        private boolean initialized = false;
        private final String defaultDeviceName;
        private String currentDeviceName;
        private long device;
        private long context;

        public AudioOutput(boolean runAudioLoop) {
            super("LavaPlayer Audio Thread");
            this.runAudioLoop = runAudioLoop;
            ALUtil.getStringList(0L, 4115);
            this.defaultDeviceName = ALC10.alcGetString(0L, 4114);
            MusicTriggers.logger.info("initialized thread");
        }

        public void setRunAudioLoop(boolean shouldPlay) {
            this.runAudioLoop = shouldPlay;
        }

        private void initializeOpenAL() {
            if (Minecraft.getInstance().options.soundDevice!=null && !Minecraft.getInstance().options.soundDevice.matches(""))
                this.currentDeviceName = Minecraft.getInstance().options.soundDevice;
            else this.currentDeviceName = this.defaultDeviceName;
            this.device = ALC10.alcOpenDevice(this.currentDeviceName);
            ALCCapabilities alccapabilities = ALC.createCapabilities(this.device);
            this.context = ALC10.alcCreateContext(this.device, (IntBuffer)null);
            ALC10.alcMakeContextCurrent(this.context);
            ALCapabilities alcapabilities = AL.createCapabilities(alccapabilities);
            if (!alcapabilities.AL_EXT_source_distance_model)
                throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
            if (!alcapabilities.AL_EXT_LINEAR_DISTANCE)
                throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
            AL10.alEnable(512);
            MusicTriggers.logger.info("Music Triggers has assumed control of device {}",this.currentDeviceName);
            int[] gen = new int[1];
            AL10.alGenSources(gen);
            this.openALSource = gen[0];
            MusicTriggers.logger.info("finished genning thread specific openAL stuff for channel {}",ChannelListener.this.channel);
        }

        private void resetOpenAL() {
            ALC10.alcDestroyContext(this.context);
            if (this.device != 0L) ALC10.alcCloseDevice(this.device);
            initializeOpenAL();
        }

        private boolean checkALError(String p_83788_) {
            int i = AL10.alGetError();
            if (i != 0) {
                MusicTriggers.logger.error("{}: {}", p_83788_, alErrorToString(i));
                return true;
            }
            return false;
        }

        private String alErrorToString(int p_83783_) {
            return switch (p_83783_) {
                case 40961 -> "Invalid name parameter.";
                case 40962 -> "Invalid enumerated parameter value.";
                case 40963 -> "Invalid parameter parameter value.";
                case 40964 -> "Invalid operation.";
                case 40965 -> "Unable to allocate memory.";
                default -> "An unrecognized error occurred.";
            };
        }

        private int removeProcessedBuffers() {
            int i = AL10.alGetSourcei(this.openALSource, 4118);
            if (i > 0) {
                int[] aint = new int[i];
                AL10.alSourceUnqueueBuffers(this.openALSource, aint);
                checkALError("Unqueue buffers");
                AL10.alDeleteBuffers(aint);
                checkALError("Remove processed buffers");
            }
            return i;
        }

        @Override
        public void run() {
            MusicTriggers.logger.info("try start run thread loop");
            try {
                if(this.runAudioLoop) {
                    MusicTriggers.logger.info("start run thread loop");
                    AudioInputStream stream = AudioPlayerInputStream.createStream(ChannelListener.this.audioPlayer, ChannelListener.this.format, ChannelListener.this.format.frameDuration(), true);
                    int bufferSize = ChannelListener.this.format.chunkSampleCount * ChannelListener.this.format.channelCount * 2;
                    byte[] buffer = new byte[bufferSize];
                    long frameDuration = ChannelListener.this.format.frameDuration();
                    while (this.runAudioLoop) {
                        if (!initialized) {
                            if (Minecraft.getInstance() != null && Minecraft.getInstance().options != null) {
                                initializeOpenAL();
                                this.initialized = true;
                            }
                        } else {
                            if (!ChannelListener.this.audioPlayer.isPaused() &&
                                    ChannelListener.this.audioPlayer.getPlayingTrack() != null &&
                                    this.initialized) {
                                int size = removeProcessedBuffers();
                                if (size == 0) {
                                    if (stream.read(buffer) >= 0) {
                                        MusicTriggers.logger.info("H");
                                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                                        int[] aint = new int[1];
                                        AL10.alGenBuffers(aint);
                                        if (!checkALError("Creating buffer")) {
                                            MusicTriggers.logger.info("HH");
                                            AL10.alBufferData(aint[0], OPEN_AL_FORMAT, byteBuffer, ChannelListener.this.format.sampleRate);
                                            if (!checkALError("Assigning buffer data")) {
                                                MusicTriggers.logger.info("HHHH");
                                                OptionalInt.of(aint[0]).ifPresent((optionalBuffer) -> AL10.alSourceQueueBuffers(this.openALSource, new int[]{optionalBuffer}));
                                            }
                                        }
                                    }
                                } else {
                                    for (int i = 0; i < removeProcessedBuffers(); i++) {
                                        MusicTriggers.logger.info(i);
                                        if (stream.read(buffer) >= 0) {
                                            MusicTriggers.logger.info("H");
                                            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                                            int[] aint = new int[1];
                                            AL10.alGenBuffers(aint);
                                            if (!checkALError("Creating buffer")) {
                                                MusicTriggers.logger.info("HH");
                                                AL10.alBufferData(aint[0], OPEN_AL_FORMAT, byteBuffer, ChannelListener.this.format.sampleRate);
                                                if (!checkALError("Assigning buffer data")) {
                                                    MusicTriggers.logger.info("HHHH");
                                                    OptionalInt.of(aint[0]).ifPresent((optionalBuffer) -> AL10.alSourceQueueBuffers(this.openALSource, new int[]{optionalBuffer}));
                                                }
                                            }
                                        } else
                                            throw new IllegalStateException("Audio stream ended. This should not happen.");
                                    }
                                }
                            } else sleep(frameDuration);
                            if (Minecraft.getInstance() != null &&
                                    Minecraft.getInstance().options != null &&
                                    Minecraft.getInstance().options.soundDevice != null &&
                                    !Minecraft.getInstance().options.soundDevice.matches("") &&
                                    !Minecraft.getInstance().options.soundDevice.matches(this.currentDeviceName))
                                resetOpenAL();
                        }
                    }
                } else {
                    sleep(250);
                    this.runAudioLoop = true;
                }
            } catch (Exception ex) {
                MusicTriggers.logger.error(ex.toString());
                for(StackTraceElement element : ex.getStackTrace()) {
                    MusicTriggers.logger.error(element.toString());
                }
            }
        }
    }
}
