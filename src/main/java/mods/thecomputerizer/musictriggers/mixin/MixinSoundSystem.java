package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SoundSystem.class)
public class MixinSoundSystem {

	@Inject(at = @At(value = "HEAD"), method = "stopSounds(Lnet/minecraft/util/Identifier;Lnet/minecraft/sound/SoundCategory;)V", cancellable = true)
	private void stopSounds(Identifier s, SoundCategory category, CallbackInfo info) {
		if(category==SoundCategory.MUSIC) {
			MusicTriggersCommon.logger.warn("Quit trying to stop my music >:(");
			info.cancel();
		}
	}
}
