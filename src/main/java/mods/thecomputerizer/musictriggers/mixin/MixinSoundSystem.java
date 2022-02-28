package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;


@Mixin(SoundSystem.class)
public class MixinSoundSystem {

	@Final
	@Shadow
	private Map<SoundInstance, Channel.SourceManager> sources;

	@Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At(value = "TAIL", target = "Lnet/minecraft/client/sound/SoundSystem;play(Lnet/minecraft/client/sound/SoundInstance;)V"))
	private void play(SoundInstance si, CallbackInfo info) {
		MusicTriggersCommon.logger.info("testing sound sys");
		MusicPlayer.sources = sources;
	}
}
