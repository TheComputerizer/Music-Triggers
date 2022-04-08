package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
@Mixin(value = SoundEngine.class, remap = false)
public class MixinSoundEngine {

    @Inject(at = @At(value = "HEAD"), method = "m_120299_(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/sounds/SoundSource;)V", cancellable = true)
    private void stop(ResourceLocation s, SoundSource category, CallbackInfo info) {
        if(category==SoundSource.MUSIC) {
            MusicTriggers.logger.warn("Quit trying to stop my music >:(");
            info.cancel();
        }
    }
}
