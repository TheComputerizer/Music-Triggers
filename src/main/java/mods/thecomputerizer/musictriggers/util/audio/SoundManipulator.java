package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import paulscode.sound.*;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;
import paulscode.sound.libraries.SourceLWJGLOpenAL;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;

public class SoundManipulator {

    public static final float MICROS_PER_SECOND = 1000000;

    public static int getMicrosecondTimeForSource(SoundSystem soundSystem, String sourceName) throws NoSuchFieldException, IllegalAccessException {
        Field libraryField = SoundSystem.class.getDeclaredField("soundLibrary");
        Field sourceMapField = Library.class.getDeclaredField("sourceMap");
        libraryField.setAccessible(true);
        sourceMapField.setAccessible(true);
        Library library = (Library) libraryField.get(soundSystem);
        HashMap<String, Source> sourceMap = (HashMap<String, Source>) sourceMapField.get(library);
        Source source = sourceMap.get(sourceName);
        if (source != null && source.channel instanceof ChannelLWJGLOpenAL) {
            ChannelLWJGLOpenAL openALChannel = (ChannelLWJGLOpenAL)source.channel;
            return (int)(openALChannel.millisecondsPlayed()*1000f);
        }
        return Integer.MAX_VALUE;
    }

    public static void setMicrosecondTimeForSource(SoundSystem soundSystem, String sourceName, float microseconds) throws NoSuchFieldException, IllegalAccessException {
        Field libraryField = SoundSystem.class.getDeclaredField("soundLibrary");
        Field sourceMapField = Library.class.getDeclaredField("sourceMap");
        libraryField.setAccessible(true);
        sourceMapField.setAccessible(true);
        Library library = (Library) libraryField.get(soundSystem);
        HashMap<String, Source> sourceMap = (HashMap<String, Source>) sourceMapField.get(library);
        Source source = sourceMap.get(sourceName);
        MusicTriggers.logger.info("Starting to set loop point");
        if (source != null && source.channel instanceof ChannelLWJGLOpenAL) {
            MusicTriggers.logger.info("Found correct channel");
            MusicTriggers.logger.info("Calculating offset: "+microseconds/MICROS_PER_SECOND);
            ChannelLWJGLOpenAL openALChannel = (ChannelLWJGLOpenAL)source.channel;
            AL10.alSourceStop(openALChannel.ALSource.get(0));
            customBuffer((SourceLWJGLOpenAL)openALChannel.attachedSource, openALChannel);
            AL10.alSourcef(openALChannel.ALSource.get(0), AL11.AL_SEC_OFFSET, (microseconds/MICROS_PER_SECOND)-openALChannel.millisecondsPlayed()*1000f);
            AL10.alSourcePlay(openALChannel.ALSource.get(0));
            openALChannel.millisPreviouslyPlayed = openALChannel.millisecondsPlayed()+microseconds/1000f;
        }
    }

    public static void customBuffer(SourceLWJGLOpenAL source, ChannelLWJGLOpenAL channel) throws NoSuchFieldException, IllegalAccessException {
        Field codecField = Source.class.getDeclaredField("codec");
        codecField.setAccessible(true);
        ICodec codec = (ICodec) codecField.get(source);
        codec.initialize(source.filenameURL.getURL());
        LinkedList<byte[]> preLoadBuffers = new LinkedList<>();
        for (int i = 0; i < SoundSystemConfig.getNumberStreamingBuffers(); i++) {
            source.soundBuffer = codec.read();
            if (source.soundBuffer == null || source.soundBuffer.audioData == null) {
                MusicTriggers.logger.error("Sound buffer was null! Could not set loop point for song!");
                break;
            }
            preLoadBuffers.add(source.soundBuffer.audioData);
        }
        source.positionChanged();
        IntBuffer streamBuffers;
        boolean playing = channel.playing();
        if (playing) AL10.alSourceStop(channel.ALSource.get(0));
        int processed = AL10.alGetSourcei(channel.ALSource.get(0), AL10.AL_BUFFERS_PROCESSED);
        if (processed > 0) {
            streamBuffers = BufferUtils.createIntBuffer(processed);
            AL10.alGenBuffers(streamBuffers);
            AL10.alSourceUnqueueBuffers(channel.ALSource.get(0), streamBuffers);
        }
        streamBuffers = BufferUtils.createIntBuffer(preLoadBuffers.size());
        AL10.alGenBuffers(streamBuffers);
        ByteBuffer byteBuffer;
        for (int i=0;i<preLoadBuffers.size();i++) {
            byteBuffer = (ByteBuffer) BufferUtils.createByteBuffer(preLoadBuffers.get(i).length).put(preLoadBuffers.get(i)).flip();
            AL10.alBufferData( streamBuffers.get(i), channel.ALformat, byteBuffer, channel.sampleRate );
        }
        AL10.alSourceQueueBuffers(channel.ALSource.get(0), streamBuffers );
    }
}
