package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.config.ConfigDebug;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
@Mixin(value = MusicManager.class, remap = false)
public class MixinMusicManager {
    @Inject(at = @At(value = "HEAD"), method = "m_120183_()V", cancellable = true)
    private void tick(CallbackInfo info) {
        if(!ConfigDebug.PLAY_NORMAL_MUSIC || ChannelManager.overridingMusicIsPlaying()) info.cancel();
    }
}