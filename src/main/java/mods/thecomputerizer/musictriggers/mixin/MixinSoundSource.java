package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.util.audio.SkippableOggAudioStream;
import net.minecraft.client.audio.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
@Mixin(value = SoundSource.class)
public abstract class MixinSoundSource{


    @Inject(at = @At("HEAD"), method = "func_216433_a(Lnet/minecraft/client/audio/IAudioStream;)V")
    private void attachBufferStream(IAudioStream stream, CallbackInfo info) {
        if (MusicPlayer.curMusic != null && MusicPlayer.curMusicSource!=null && ((SoundSource) (Object) this).source == MusicPlayer.curMusicSource.source) {
            if (!(((SoundSource) (Object) this).stream instanceof SkippableOggAudioStream))
                MusicPlayer.curMusicTimer = 0;
        }
    }
}
