package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import net.minecraft.client.audio.MusicTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MusicTicker.class, remap = false)
public class MixinMusicTicker {

    @Inject(at = @At(value = "HEAD"), method = "update()V", cancellable = true)
    private void update(CallbackInfo info) {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC || ChannelManager.canAnyChannelOverrideMusic()) info.cancel();
    }
}
