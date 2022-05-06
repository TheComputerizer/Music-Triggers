package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import paulscode.sound.*;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

import java.lang.reflect.Field;
import java.util.HashMap;

@SuppressWarnings("unchecked")
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
            return (int)(openALChannel.millisecondsPlayed()*source.getPitch());
        }
        return Integer.MAX_VALUE;
    }

    public static void setMillisecondTimeForSource(SoundSystem soundSystem, String sourceName, float milliseconds)
    {
        if(sourceName!=null) {
            try {
                Field libraryField = SoundSystem.class.getDeclaredField("soundLibrary");
                libraryField.setAccessible(true);
                Library library = (Library) libraryField.get(soundSystem);
                MusicTriggers.logger.info("Starting to set loop point");
                CommandSetTime.setMillisecondTime(soundSystem, (LibraryLWJGLOpenAL) library, sourceName, milliseconds);
            } catch (Exception e) {
                MusicTriggers.logger.fatal("Unable to set loop position!");
                e.printStackTrace();
            }
        } else MusicTriggers.logger.info("null source");
    }
}
