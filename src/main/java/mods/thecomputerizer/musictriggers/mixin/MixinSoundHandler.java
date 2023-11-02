package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundHandler.class)
public class MixinSoundHandler {

    @Inject(at = @At("RETURN"), method = "updateSourceVolume")
    private void musictriggers$updateSourceVolume(SoundCategory category, float volume, CallbackInfo ci) {
        ChannelManager.distributeSoundUpdate(category,volume);
    }
}
