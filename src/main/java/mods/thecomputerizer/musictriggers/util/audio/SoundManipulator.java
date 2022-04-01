package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.Source;
import org.lwjgl.openal.AL10;

public class SoundManipulator {

    public static void setMillisecondTimeForSource(SoundInstance sound, long milliseconds)
    {
        if (sound != null) {
            MinecraftClient.getInstance().getSoundManager().soundSystem.channel.executor.execute(() -> {
                SoundManager sh = MinecraftClient.getInstance().getSoundManager();
                try {
                    Source source = sh.soundSystem.sources.get(sound).source;
                    assert source != null;
                    assert source.stream != null;
                    AL10.alSourceStop(source.pointer);
                    source.stream.close();
                    source.removeProcessedBuffers();
                    source.setStream(new SkippableOggAudioStream(sh.soundSystem.soundLoader.resourceManager.getResource(sound.getSound().getLocation()).getInputStream(), milliseconds));
                    AL10.alSourcePlay(source.pointer);
                } catch (Exception e) {
                    MusicTriggersCommon.logger.error("Failed to seek to position in song!");
                    e.printStackTrace();
                }
            });
        }
    }
}
