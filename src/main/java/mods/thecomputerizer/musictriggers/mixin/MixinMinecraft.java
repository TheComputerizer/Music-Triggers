package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At("RETURN"), method = "reloadResourcePacks")
    private void musictrigger_refreshResources(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ChannelManager.checkResourceReload();
    }
}