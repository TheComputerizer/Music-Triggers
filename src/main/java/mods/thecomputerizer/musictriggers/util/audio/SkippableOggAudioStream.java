package mods.thecomputerizer.musictriggers.util.audio;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.OggAudioStream;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

@SuppressWarnings("ConstantConditions")
public class SkippableOggAudioStream extends OggAudioStream {

    public long bytesToSkip;
    public long milliseconds;
    public long bytesRead;

    public SkippableOggAudioStream(InputStream stream, long milliseconds) throws IOException {
        super(stream);
        this.milliseconds = milliseconds;
        this.bytesToSkip = (long)((40f/3f)*milliseconds);
        this.bytesRead = 0;
    }

    protected boolean readFrame(@NotNull OggAudioStream.OutputConcat output) throws IOException {
        if (this.handle == 0L) {
            return false;
        } else {
            MemoryStack memorystack = MemoryStack.stackPush();

            boolean flag1;
            label79: {
                boolean flag;
                label80: {
                    try {
                        PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
                        IntBuffer intbuffer = memorystack.mallocInt(1);
                        IntBuffer intbuffer1 = memorystack.mallocInt(1);
                        while(true) {
                            int l = STBVorbis.stb_vorbis_decode_frame_pushdata(this.handle, this.buffer, intbuffer, pointerbuffer, intbuffer1);
                            this.buffer.position(this.buffer.position() + l);
                            int i = STBVorbis.stb_vorbis_get_error(this.handle);
                            if (i == 1) {
                                this.forwardBuffer();
                                if (!this.refillFromStream()) {
                                    flag1 = false;
                                    break label79;
                                }
                            } else {
                                if (i != 0) {
                                    throw new IOException("Failed to read Ogg file " + i);
                                }
                                if(this.milliseconds!=0) {
                                    if (this.bytesRead+l <= this.bytesToSkip) {
                                        this.bytesRead+=l;
                                        flag = true;
                                    } else {
                                        MusicPlayer.curMusicTimer = this.milliseconds;
                                        this.milliseconds=0;
                                        this.bytesRead=0;
                                    }
                                }
                                if(this.milliseconds==0) {
                                    int j = intbuffer1.get(0);
                                    if (j != 0) {
                                        int k = intbuffer.get(0);
                                        PointerBuffer pointerbuffer1 = pointerbuffer.getPointerBuffer(k);
                                        if (k == 1) {
                                            this.convertMono(pointerbuffer1.getFloatBuffer(0, j), output);
                                            flag = true;
                                            break label80;
                                        }
                                        if (k != 2) {
                                            throw new IllegalStateException("Invalid number of channels: " + k);
                                        }
                                        this.convertStereo(pointerbuffer1.getFloatBuffer(0, j), pointerbuffer1.getFloatBuffer(1, j), output);
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Throwable throwable1) {
                        if (memorystack != null) {
                            try {
                                memorystack.close();
                            } catch (Throwable throwable) {
                                throwable1.addSuppressed(throwable);
                            }
                        }
                        throw throwable1;
                    }
                    if (memorystack != null) {
                        memorystack.close();
                    }
                    return flag;
                }
                if (memorystack != null) {
                    memorystack.close();
                }
                return flag;
            }
            if (memorystack != null) {
                memorystack.close();
            }
            return flag1;
        }
    }
}
