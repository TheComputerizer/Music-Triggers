package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import net.minecraft.client.sound.OggAudioStream;
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
        this.bytesToSkip = (long)((44.1f)*milliseconds);
        this.bytesRead = 0;
    }

    @Override
    protected boolean readOggFile(@NotNull ChannelList output) throws IOException {
        if (this.pointer == 0L) {
            return false;
        }
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            block14:
            {
                int k;
                PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                IntBuffer intBuffer = memoryStack.mallocInt(1);
                IntBuffer intBuffer2 = memoryStack.mallocInt(1);
                while (true) {
                    int i = STBVorbis.stb_vorbis_decode_frame_pushdata(this.pointer, this.buffer, intBuffer, pointerBuffer, intBuffer2);
                    this.buffer.position(this.buffer.position() + i);
                    int j = STBVorbis.stb_vorbis_get_error(this.pointer);
                    if (j == 1) {
                        this.increaseBufferSize();
                        if (this.readHeader()) continue;
                        break block14;
                    }
                    if (j != 0) {
                        throw new IOException("Failed to read Ogg file " + j);
                    }
                    k = intBuffer2.get(0);
                    if (k != 0) break;
                }
                if(this.milliseconds!=0) {
                    if (this.bytesRead+k <= this.bytesToSkip) {
                        this.bytesRead+=k;
                        return true;
                    } else {
                        MusicPlayer.curMusicTimer = this.milliseconds;
                        this.milliseconds=0;
                        this.bytesRead=0;
                    }
                }
                int l = intBuffer.get(0);
                PointerBuffer pointerBuffer2 = pointerBuffer.getPointerBuffer(l);
                if (l == 1) {
                    this.readChannels(pointerBuffer2.getFloatBuffer(0, k), output);
                    boolean bl = true;
                    return bl;
                }
                if (l == 2) {
                    this.readChannels(pointerBuffer2.getFloatBuffer(0, k), pointerBuffer2.getFloatBuffer(1, k), output);
                    boolean bl = true;
                    return bl;
                }
                throw new IllegalStateException("Invalid number of channels: " + l);
            }
            boolean bl = false;
            return bl;
        }
    }
}
