package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import org.lwjgl.openal.AL10;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.Source;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.libraries.SourceLWJGLOpenAL;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class CommandSetTime {

    public static void setMillisecondTime(SoundSystem soundSystem, LibraryLWJGLOpenAL library, String sourcename, float milliseconds) throws NoSuchFieldException, IllegalAccessException {
        soundSystem.CommandQueue(null);
        synchronized(SoundSystemConfig.THREAD_SYNC) {
            Field sourceMapField = Library.class.getDeclaredField("sourceMap");
            sourceMapField.setAccessible(true);
            HashMap<String, Source> sourceMap = (HashMap<String, Source>) sourceMapField.get(library);
            Source src = sourceMap.get(sourcename);
            if(src!=null) {
                ChannelLWJGLOpenAL channel = (ChannelLWJGLOpenAL) src.channel;
                library.stop(sourcename);
                if (!(src instanceof SourceLWJGLOpenALExtension))
                    upcastSource((SourceLWJGLOpenAL) src, sourcename, sourceMap, milliseconds, src.sourceVolume, src.getPitch());
                else ((SourceLWJGLOpenALExtension) src).milliseconds = milliseconds;
                library.play(sourcename);
                if (AL10.alGetError() == AL10.AL_NO_ERROR)
                    channel.millisPreviouslyPlayed = (milliseconds / src.getPitch());
                MusicTriggers.logger.debug("Attempted to set track to " + milliseconds + " milliseconds");
            } else MusicTriggers.logger.warn("Tried to set the time for a null source!");
        }
    }

    public static void upcastSource(SourceLWJGLOpenAL source, String sourcename, HashMap<String, Source> sourceMap, float milliseconds, float volume, float pitch) throws NoSuchFieldException, IllegalAccessException {
        Field listenerPositionField = SourceLWJGLOpenAL.class.getDeclaredField("listenerPosition");
        listenerPositionField.setAccessible(true);
        FloatBuffer listenerPosition = (FloatBuffer)listenerPositionField.get(source);
        Field myBufferField = SourceLWJGLOpenAL.class.getDeclaredField("myBuffer");
        myBufferField.setAccessible(true);
        IntBuffer myBuffer = (IntBuffer)myBufferField.get(source);
        sourceMap.put(sourcename, new SourceLWJGLOpenALExtension(listenerPosition, myBuffer, source.priority, source.toStream, source.toLoop, sourcename, source.filenameURL, null, source.position.x, source.position.y, source.position.z, source.attModel, source.distOrRoll, false, milliseconds, volume, pitch));
    }
}
