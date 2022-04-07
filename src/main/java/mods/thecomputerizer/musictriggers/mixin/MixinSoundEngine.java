package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
@Mixin(value = SoundEngine.class, remap = false)
public class MixinSoundEngine {

    @Inject(at = @At(value = "HEAD"), method = "func_195855_a(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/util/SoundCategory;)V", cancellable = true)
    private void stop(ResourceLocation s, SoundCategory category, CallbackInfo info) {
        if(category==SoundCategory.MUSIC) {
            MusicTriggers.logger.warn("Quit trying to stop my music >:(");
            info.cancel();
        }
    }
}
