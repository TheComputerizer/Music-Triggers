package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerBossInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerBossInfo.class)
public class MixinServerBossInfo {

    @Shadow
    private boolean visible;

    @Inject(at = @At(value = "HEAD"), method = "addPlayer")
    private void musictriggers_addPlayer(ServerPlayerEntity player, CallbackInfo ci) {
        if(this.visible)
            ServerChannels.addBossBarTracking(player.getUUID(),(ServerBossInfo)(Object)this);
    }

    @Inject(at = @At(value = "HEAD"), method = "removePlayer")
    private void musictriggers_removePlayer(ServerPlayerEntity player, CallbackInfo ci) {
        ServerChannels.removeBossBarTracking(player.getUUID(),(ServerBossInfo)(Object)this);
    }

}
