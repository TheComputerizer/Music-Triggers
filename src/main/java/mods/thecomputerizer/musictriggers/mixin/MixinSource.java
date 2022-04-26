package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.util.audio.SkippableOggAudioStream;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = Source.class)
public abstract class MixinSource{

    @Inject(at = @At("HEAD"), method = "setStream(Lnet/minecraft/client/sound/AudioStream;)V")
    private void setStream(AudioStream stream, CallbackInfo info) {
        if (MusicPlayer.curMusic != null && MusicPlayer.curMusicSource!=null && ((Source) (Object) this).pointer == MusicPlayer.curMusicSource.pointer) {
            if (!(((Source) (Object) this).stream instanceof SkippableOggAudioStream))
                MusicPlayer.curMusicTimer = 0;
        }
    }
}
