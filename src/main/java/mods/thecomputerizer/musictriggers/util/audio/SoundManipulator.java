package mods.thecomputerizer.musictriggers.util.audio;

import com.mojang.blaze3d.audio.Channel;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.lwjgl.openal.AL10;

public class SoundManipulator {

    public static void setMillisecondTimeForSource(SoundInstance sound, long milliseconds)
    {
        if (sound != null) {
            Minecraft.getInstance().getSoundManager().soundEngine.channelAccess.executor.execute(() -> {
                SoundManager sh = Minecraft.getInstance().getSoundManager();
                try {
                    Channel channel = sh.soundEngine.instanceToChannel.get(sound).channel;
                    assert channel != null;
                    assert channel.stream != null;
                    AL10.alSourceStop(channel.source);
                    channel.stream.close();
                    channel.removeProcessedBuffers();
                    channel.attachBufferStream(new SkippableOggAudioStream(sh.soundEngine.soundBuffers.resourceManager.getResource(sound.getSound().getPath()).getInputStream(), milliseconds));
                    AL10.alSourcePlay(channel.source);
                } catch (Exception e) {
                    MusicTriggers.logger.error("Failed to seek to position in song!");
                    e.printStackTrace();
                }
            });
        }
    }
}
