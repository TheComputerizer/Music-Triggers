package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.ClientEvents;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;getSound()Lnet/minecraft/client/resources/sounds/Sound;"), method = "play")
    private Sound musictriggers_getSoundRedirect(SoundInstance sound) {
        return ClientEvents.playSound(sound).getSound();
    }
}
