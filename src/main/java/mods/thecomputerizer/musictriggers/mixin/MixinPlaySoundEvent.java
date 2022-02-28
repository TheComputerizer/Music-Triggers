package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.util.events.PlaySoundEvent;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class MixinPlaySoundEvent {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At(value = "HEAD", target = "Lnet/minecraft/client/sound/SoundSystem;play(Lnet/minecraft/client/sound/SoundInstance;)V"))
    private void play(SoundInstance si, CallbackInfo info) {
        MusicTriggersCommon.logger.info("testing sound event");
        PlaySoundEvent.EVENT.invoker().interact(si);
    }
}
