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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.OptionalInt;

@SuppressWarnings("deprecation")
@OnlyIn(value = Dist.CLIENT)
public class ChannelListener extends AudioEventAdapter {
    private final AudioPlayer audioPlayer;
    private final AudioDataFormat format;

    private final AudioOutput AUDIO_THREAD;
    private final String channel;

    public ChannelListener(AudioPlayer audioPlayer, AudioDataFormat format, String channel) {
        this.audioPlayer = audioPlayer;
        this.format = format;
        this.channel = channel;
        this.AUDIO_THREAD = new AudioOutput(true,channel);
        this.audioPlayer.addListener(this);
        this.AUDIO_THREAD.start();
        MusicTriggers.logger.info("started thread");
    }

    public void stopThread() {
        this.AUDIO_THREAD.stop();
    }

    public void pauseChannelAudio() {
        this.AUDIO_THREAD.pauseChannelAudio();
    }

    public void unPauseChannelAudio() {
        this.AUDIO_THREAD.unPauseChannelAudio();
    }

    public void stopChannelAudio() {
        this.AUDIO_THREAD.stopChannelAudio();
    }

    public void setChannelVolume(float volume) {
        this.AUDIO_THREAD.setChannelVolume(volume);
    }

    public void setChannelPitch(float pitch) {
        this.AUDIO_THREAD.setChannelPitch(pitch);
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
        private final String channelName;
        private boolean runAudioLoop;
        private int openALSource;
        private boolean initialized;
        private boolean queueInitialized;
        private boolean errored;
        private String defaultDeviceName;
        private String currentDeviceName;
        private long device;
        private long context;

        private float volume;
        private float pitch;

        public AudioOutput(boolean runAudioLoop, String channelName) {
            super("LavaPlayer Audio Thread");
            this.channelName = channelName;
            this.runAudioLoop = runAudioLoop;
            this.initialized = false;
            this.queueInitialized = false;
            this.errored = false;
            this.defaultDeviceName = "";
            this.volume = 1f;
            this.pitch = 1f;
            ALUtil.getStringList(0L, 4115);
            this.defaultDeviceName = ALC10.alcGetString(0L, 4114);
            MusicTriggers.logger.info("initialized thread");
        }

        public void setRunAudioLoop(boolean shouldPlay) {
            this.runAudioLoop = shouldPlay;
        }

        private int getALState() {
            if(!(this.initialized && this.runAudioLoop && !this.errored)) return 4116;
            int ret = AL10.alGetSourcei(this.openALSource, 4112);
            checkALError("Getting state of channel audio ("+this.channelName+")");
            return ret;
        }

        private void playChannelAudio() {
            AL10.alSourcePlay(this.openALSource);
            checkALError("Playing channel audio ("+this.channelName+")");
        }

        private void pauseChannelAudio() {
            if (this.getALState() == 4114) {
                AL10.alSourcePause(this.openALSource);
                checkALError("Pausing channel audio ("+this.channelName+")");
            }
        }

        private void unPauseChannelAudio() {
            if (this.getALState() == 4115) {
                AL10.alSourcePlay(this.openALSource);
                checkALError("Unpausing channel audio ("+this.channelName+")");
            }
        }

        private void stopChannelAudio() {
            if (this.queueInitialized) {
                int state = this.getALState();
                if(state==4115 || state==4114) {
                    AL10.alSourceStop(this.openALSource);
                    checkALError("Stopping channel audio (" + this.channelName + ")");
                }
            }
        }

        private void setChannelVolume(float volume) {
            this.volume = volume;
        }

        private void setSourceVolume() {
            AL10.alSourcef(this.openALSource, 4106, this.volume);
            checkALError("Setting channel volume ("+this.channelName+") to "+this.volume);
        }

        private void setChannelPitch(float pitch) {
            this.pitch = pitch;
        }

        private void setSourcePitch() {
            AL10.alSourcef(this.openALSource, 4099, this.pitch);
            checkALError("Setting channel pitch ("+this.channelName+") to "+this.pitch);
        }

        private void initializeOpenAL() {
            if (Minecraft.getInstance().options.soundDevice != null && !Minecraft.getInstance().options.soundDevice.matches(""))
                this.currentDeviceName = Minecraft.getInstance().options.soundDevice;
            else this.currentDeviceName = this.defaultDeviceName;
            this.device = ALC10.alcOpenDevice(this.currentDeviceName);
            ALCCapabilities alccapabilities = ALC.createCapabilities(this.device);
            if (checkALCError(this.device, "Get capabilities")) {
                throw new IllegalStateException("Failed to get OpenAL capabilities for channel "+this.channelName);
            } else if (!alccapabilities.OpenALC11)
                throw new IllegalStateException("OpenAL 1.1 not supported");
            else {
                this.context = ALC10.alcCreateContext(this.device, (IntBuffer) null);
                ALC10.alcMakeContextCurrent(this.context);
                ALCapabilities alcapabilities = AL.createCapabilities(alccapabilities);
                if (!checkALError("initialize AL Capabilities")) {
                    if (!alcapabilities.AL_EXT_source_distance_model)
                        throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
                    if (!alcapabilities.AL_EXT_LINEAR_DISTANCE)
                        throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
                    AL10.alEnable(512);
                    if(alcapabilities.AL_EXT_LINEAR_DISTANCE) {
                        if (!checkALError("Enabled Capabilities")) {
                            MusicTriggers.logger.info("Music Triggers has assumed control of device {}", this.currentDeviceName);
                            int[] gen = new int[1];
                            AL10.alGenSources(gen);
                            if (!checkALError("Gen sources")) {
                                this.openALSource = gen[0];
                                MusicTriggers.logger.info("finished genning thread specific openAL stuff for channel {}", ChannelListener.this.channel);
                                this.initialized = true;
                                return;
                            }
                        }
                    } else MusicTriggers.logger.error("AL_EXT_LINEAR_DISTANCE somehow got initialized?");
                }
            }
            this.errored = true;
        }

        private void resetOpenAL() {
            if(this.queueInitialized) {
                drainQueuedBuffers();
                this.queueInitialized = false;
            }
            ALC10.alcDestroyContext(this.context);
            if (this.device != 0L) ALC10.alcCloseDevice(this.device);
            if (!this.errored) initializeOpenAL();
        }

        private boolean checkALError(String message) {
            int i = AL10.alGetError();
            if (i != 0) {
                MusicTriggers.logger.error("{}: {}", message, alErrorToString(i));
                return true;
            } return false;
        }

        private boolean checkALCError(long id, String message) {
            int i = ALC10.alcGetError(id);
            if (i != 0) {
                MusicTriggers.logger.error("{}{}: {}", message, id, alcErrorToString(i));
                return true;
            } return false;
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

        private String alcErrorToString(int p_83792_) {
            return switch (p_83792_) {
                case 40961 -> "Invalid device.";
                case 40962 -> "Invalid context.";
                case 40963 -> "Illegal enum.";
                case 40964 -> "Invalid value.";
                case 40965 -> "Unable to allocate memory.";
                default -> "An unrecognized error occurred.";
            };
        }

        private int removeProcessedBuffers() {
            int i = AL10.alGetSourcei(this.openALSource, 4118);
            if(!checkALError("Get processed buffers")) {
                if (i > 0) {
                    int[] aint = new int[i];
                    AL10.alSourceUnqueueBuffers(this.openALSource, aint);
                    checkALError("Unqueue buffers");
                    AL10.alDeleteBuffers(aint);
                    checkALError("Remove processed buffers");
                }
                return i;
            } return 0;
        }

        private void drainQueuedBuffers() {
            int i = AL10.alGetSourcei(this.openALSource, 4117);
            if(!checkALError("Get queued buffers") && i > 0) {
                int[] aint = new int[i];
                AL10.alSourceUnqueueBuffers(this.openALSource, aint);
                checkALError("Unqueue buffers");
                AL10.alDeleteBuffers(aint);
                checkALError("Remove processed buffers");
            }
        }

        private void queueBuffers(AudioInputStream stream, byte[] buffer, int num) throws IOException {
            MusicTriggers.logger.info("Queuing {} buffers in channel {}",num,this.channelName);
            for(int i=0;i<num;i++) {
                if (stream.read(buffer) >= 0) {
                    MusicTriggers.logger.info("Buffer {}",i+1);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                    int[] aint = new int[1];
                    AL10.alGenBuffers(aint);
                    if (!checkALError("Generating buffer")) {
                        MusicTriggers.logger.info("Pushing buffer {} to OpenAL",i+1);
                        AL10.alBufferData(aint[0], AL10.AL_FORMAT_STEREO16, byteBuffer, ChannelListener.this.format.sampleRate);
                        if (!checkALError("Assigning buffer data")) {
                            MusicTriggers.logger.info("Successfully queuing buffer {}",i+1);
                            OptionalInt.of(aint[0]).ifPresent((optionalBuffer) -> AL10.alSourceQueueBuffers(this.openALSource, new int[]{optionalBuffer}));
                        }
                    }
                } else {
                    MusicTriggers.logger.fatal("Audio stream ended for channel {}! This should not happen! Attempting to restart.",this.channelName);
                    setRunAudioLoop(false);
                }
            }
        }

        @Override
        public void run() {
            if(!this.errored) {
                try {
                    if (this.runAudioLoop) {
                        MusicTriggers.logger.info("start run thread loop ({})",this.channelName);
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
                                if (!ChannelListener.this.audioPlayer.isPaused() && ChannelListener.this.audioPlayer.getPlayingTrack() != null) {
                                    if(!this.queueInitialized) {
                                        queueBuffers(stream,buffer,3);
                                        this.queueInitialized = true;
                                        setSourcePitch();
                                        setSourceVolume();
                                        playChannelAudio();
                                    } else {
                                        int size = removeProcessedBuffers();
                                        if(size>0) MusicTriggers.logger.info("Successfully processed and removed {} buffers! Attempting to refill the queue.", size);
                                        queueBuffers(stream, buffer, size);
                                    }
                                } else {
                                    if(this.queueInitialized) {
                                        drainQueuedBuffers();
                                        this.queueInitialized = false;
                                    }
                                    sleep(frameDuration);
                                }
                                if (Minecraft.getInstance() != null && Minecraft.getInstance().options != null && Minecraft.getInstance().options.soundDevice != null &&
                                        !Minecraft.getInstance().options.soundDevice.matches("") && !Minecraft.getInstance().options.soundDevice.matches(this.currentDeviceName))
                                    resetOpenAL();
                            }
                        }
                    } else {
                        sleep(250);
                        if (Minecraft.getInstance() != null && Minecraft.getInstance().options != null && Minecraft.getInstance().options.soundDevice != null && this.initialized)
                            resetOpenAL();
                        this.runAudioLoop = true;
                    }
                } catch (Exception ex) {
                    MusicTriggers.logger.error(ex.toString());
                    for (StackTraceElement element : ex.getStackTrace()) MusicTriggers.logger.error(element.toString());
                }
            } else {
                MusicTriggers.logger.fatal("ERROR THROWN IN AUDIO CHANNEL {}",ChannelListener.this.channel);
                stop();
            }
        }
    }
}
