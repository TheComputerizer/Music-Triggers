package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import net.minecraft.client.sound.MusicTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
@Mixin(value = MusicTracker.class, remap = false)
public class MixinMusicTracker {

    @Inject(at = @At(value = "HEAD"), method = "method_18669()V", cancellable = true)
    private void tick(CallbackInfo info) {
        if(!ConfigDebug.SilenceIsBad || !ChannelManager.canAnyChannelOverrideMusic()) info.cancel();
    }
}
