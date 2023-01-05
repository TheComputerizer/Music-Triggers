package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SoundEngine.class)
public class MixinPlaySoundEvent {

    @ModifyVariable(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At(value = "HEAD", target = "Lnet/minecraft/client/sound/SoundSystem;play(Lnet/minecraft/client/sound/SoundInstance;)V"), ordinal = 0, argsOnly = true)
    private SoundInstance play(SoundInstance sound2) {
        return EventsClient.playSound(sound2);
    }
}
