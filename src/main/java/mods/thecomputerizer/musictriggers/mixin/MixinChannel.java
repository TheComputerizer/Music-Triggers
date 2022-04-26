package mods.thecomputerizer.musictriggers.mixin;

import com.mojang.blaze3d.audio.Channel;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.util.audio.SkippableOggAudioStream;
import net.minecraft.client.sounds.AudioStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
@Mixin(value = Channel.class)
public abstract class MixinChannel{

    @Inject(at = @At("HEAD"), method = "m_83658_(Lnet/minecraft/client/sounds/AudioStream;)V")
    private void attachBufferStream(AudioStream stream, CallbackInfo info) {
        if (MusicPlayer.curMusic != null && MusicPlayer.curMusicSource!=null && ((Channel) (Object) this).source == MusicPlayer.curMusicSource.source) {
            if (!(((Channel) (Object) this).stream instanceof SkippableOggAudioStream))
                MusicPlayer.curMusicTimer = 0;
        }
    }
}
