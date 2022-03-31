package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import net.minecraft.client.audio.OggAudioStream;
import net.minecraft.client.audio.SoundSource;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

public class SkippableOggAudioStream extends OggAudioStream {

    public long bytesToSkip;
    public long milliseconds;
    public long bytesRead;

    public SkippableOggAudioStream(InputStream stream, long milliseconds) throws IOException {
        super(stream);
        this.milliseconds = milliseconds;
        float bytesPerMilli = (float)SoundSource.calculateBufferSize(this.getFormat(),1)/8088f;
        this.bytesToSkip = (long)(bytesPerMilli*milliseconds);
        this.bytesRead = 0;
    }

    @Override
    protected boolean readFrame(OggAudioStream.Buffer p_216460_1_) throws IOException {
        if (this.handle == 0L) {
            return false;
        } else {
            try (MemoryStack memorystack = MemoryStack.stackPush()) {
                PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
                IntBuffer intbuffer = memorystack.mallocInt(1);
                IntBuffer intbuffer1 = memorystack.mallocInt(1);

                while(true) {
                    int i = STBVorbis.stb_vorbis_decode_frame_pushdata(this.handle, this.buffer, intbuffer, pointerbuffer, intbuffer1);
                    this.buffer.position(this.buffer.position() + i);
                    int j = STBVorbis.stb_vorbis_get_error(this.handle);
                    if (j == 1) {
                        this.forwardBuffer();
                        if (!this.refillFromStream()) {
                            return false;
                        }
                    } else {
                        if (j != 0) {
                            throw new IOException("Failed to read Ogg file " + j);
                        }
                        if(this.milliseconds!=0) {
                            if (this.bytesRead + i <= this.bytesToSkip) {
                                this.bytesRead+=i;
                                return true;
                            } else {
                                MusicPlayer.curMusicTimer = this.milliseconds;
                                this.milliseconds=0;
                                this.bytesRead=0;
                            }
                        }
                        int k = intbuffer1.get(0);
                        if (k != 0) {
                            int l = intbuffer.get(0);
                            PointerBuffer pointerbuffer1 = pointerbuffer.getPointerBuffer(l);
                            if (l != 1) {
                                if (l == 2) {
                                    this.convertStereo(pointerbuffer1.getFloatBuffer(0, k), pointerbuffer1.getFloatBuffer(1, k), p_216460_1_);
                                    return true;
                                }

                                throw new IllegalStateException("Invalid number of channels: " + l);
                            }

                            this.convertMono(pointerbuffer1.getFloatBuffer(0, k), p_216460_1_);
                            return true;
                        }
                    }
                }
            }
        }
    }
}
