package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundSystem;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class SoundManipulator {

    public static void setMillisecondTimeForSource(ISound sound, String songname, long milliseconds)
    {
        if (sound != null) {
            try {
                Objects.requireNonNull(Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel.get(sound).channel).stop();
                ((SoundSystem.HandlerImpl)Minecraft.getInstance().getSoundManager().soundEngine.library.streamingChannels).release(Objects.requireNonNull(Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel.get(sound).channel));
                InputStream buff = Files.newInputStream(Paths.get("./config/MusicTriggers/songs/assets/musictriggers/sounds/music/" + songname + ".ogg"));
                Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel.get(sound).channel = ((SoundSystem.HandlerImpl)Minecraft.getInstance().getSoundManager().soundEngine.library.streamingChannels).acquire();
                Objects.requireNonNull(Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel.get(sound).channel).attachBufferStream(new SkippableOggAudioStream(buff, milliseconds));
                Objects.requireNonNull(Minecraft.getInstance().getSoundManager().soundEngine.instanceToChannel.get(sound).channel).play();
            } catch (Exception e) {
                MusicTriggers.logger.error("Failed to seek to position in song!");
                e.printStackTrace();
            }
        }
    }
}
