package mods.thecomputerizer.musictriggers.mixin;

import com.legacy.blue_skies.client.audio.SkiesMusicTicker;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = SkiesMusicTicker.class)
public class MixinBlueSkiesMusic {

    @Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
    private void musictriggers_tick(CallbackInfo info) {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC) info.cancel();
    }
}
