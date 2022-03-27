package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import paulscode.sound.*;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;

import java.lang.reflect.Field;
import java.util.HashMap;

public class SoundManipulator {

    public static int getMillisecondTimeForSource(SoundSystem soundSystem, String sourceName) throws NoSuchFieldException, IllegalAccessException {
        Field libraryField = SoundSystem.class.getDeclaredField("soundLibrary");
        Field sourceMapField = Library.class.getDeclaredField("sourceMap");
        libraryField.setAccessible(true);
        sourceMapField.setAccessible(true);
        Library library = (Library) libraryField.get(soundSystem);
        HashMap<String, Source> sourceMap = (HashMap<String, Source>) sourceMapField.get(library);
        Source source = sourceMap.get(sourceName);
        if (source != null && source.channel instanceof ChannelLWJGLOpenAL) {
            ChannelLWJGLOpenAL openALChannel = (ChannelLWJGLOpenAL)source.channel;
            return (int)(openALChannel.millisecondsPlayed());
        }
        return Integer.MAX_VALUE;
    }

    public static void setMillisecondTimeForSource(SoundSystem soundSystem, String sourceName, float milliseconds)
    {
        try
        {
            Field libraryField = SoundSystem.class.getDeclaredField("soundLibrary");
            Field sourceMapField = Library.class.getDeclaredField("sourceMap");
            libraryField.setAccessible(true);
            sourceMapField.setAccessible(true);
            Library library = (Library) libraryField.get(soundSystem);
            HashMap<String, Source> sourceMap = (HashMap<String, Source>) sourceMapField.get(library);
            Source src = sourceMap.get(sourceName);
            MusicTriggers.logger.info("Starting to set loop point");
            if (src != null && src.channel instanceof ChannelLWJGLOpenAL) {
                soundSystem.CommandQueue(null);
                final ChannelLWJGLOpenAL c = (ChannelLWJGLOpenAL) src.channel;
                final boolean play = src.playing();
                AL10.alSourceRewind(c.ALSource.get(0));
                AL10.alSourcef(c.ALSource.get(0), AL11.AL_SEC_OFFSET, milliseconds / 1000);
                if (AL10.alGetError() == AL10.AL_NO_ERROR) c.millisPreviouslyPlayed = milliseconds;
                if (play && src.toStream) {
                    src.stop();
                    src.play(c);
                }
            }
            else MusicTriggers.logger.error("Bad loop source!");
        }
        catch (final Exception ex) { ex.printStackTrace(); }
    }
}
