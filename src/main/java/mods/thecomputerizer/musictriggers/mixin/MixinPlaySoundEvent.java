package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.eventsClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SoundSystem.class)
public class MixinPlaySoundEvent {

    @ModifyVariable(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At(value = "HEAD", target = "Lnet/minecraft/client/sound/SoundSystem;play(Lnet/minecraft/client/sound/SoundInstance;)V"), ordinal = 0, argsOnly = true)
    private SoundInstance play(SoundInstance sound2) {
        MusicTriggersCommon.logger.info("play sound event");
        return eventsClient.playSound(sound2);
    }
}
