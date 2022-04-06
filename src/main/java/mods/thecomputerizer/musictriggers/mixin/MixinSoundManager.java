package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
@Mixin(value = SoundManager.class, remap = false)
public class MixinSoundManager {

    @Inject(at = @At(value = "HEAD"), method = "func_189567_a(Ljava/lang/String;Lnet/minecraft/util/SoundCategory;)V", cancellable = true)
    private void stop(String s, SoundCategory category, CallbackInfo info) {
        if(category==SoundCategory.MUSIC) {
            MusicTriggers.logger.warn("Quit trying to stop my music >:(");
            info.cancel();
        }
    }
}
