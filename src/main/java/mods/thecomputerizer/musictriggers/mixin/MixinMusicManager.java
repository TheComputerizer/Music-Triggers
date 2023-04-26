package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This is what stops vanilla music from overlapping when necessary
 * The max priority ensures that this wins over other mods that mixin into the same class
 */
@Mixin(value = MusicManager.class, priority = Integer.MAX_VALUE)
public class MixinMusicManager {

    @Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
    private void musictriggers_tick(CallbackInfo info) {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC) info.cancel();
    }
}
