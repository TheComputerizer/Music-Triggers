package mods.thecomputerizer.musictriggers.mixin;

import com.legacy.blue_skies.BlueSkies;
import com.legacy.blue_skies.client.audio.SkiesMusicTicker;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@SuppressWarnings("ALL")
@Pseudo
@Mixin(value = SkiesMusicTicker.class)
public class MixinBlueSkiesMusic {

    @Inject(at = @At(value = "HEAD"), method = "func_110550_d", cancellable = true)
    private void tick(CallbackInfo info) {
        if(Arrays.asList(ConfigDebug.blockedmods).contains(BlueSkies.MODID) && (!ConfigDebug.SilenceIsBad || ChannelManager.canAnyChannelOverrideMusic())) info.cancel();
    }
}
