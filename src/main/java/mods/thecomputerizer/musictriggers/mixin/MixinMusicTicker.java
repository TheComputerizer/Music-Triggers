package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import net.minecraft.client.audio.MusicTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This is what stops vanilla music from overlapping when necessary
 * The max priority ensures that this wins over other mods that mixin into the same class
 */
@Mixin(value = MusicTicker.class, priority = Integer.MAX_VALUE)
public class MixinMusicTicker {

    @Inject(at = @At(value = "HEAD"), method = "update", cancellable = true)
    private void musictriggers_update(CallbackInfo info) {
        if(ChannelManager.checkMusicTickerCancel()) info.cancel();
    }
}
