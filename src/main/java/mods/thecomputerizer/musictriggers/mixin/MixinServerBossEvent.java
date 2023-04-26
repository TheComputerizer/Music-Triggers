package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerBossEvent.class)
public class MixinServerBossEvent {

    @Shadow
    private boolean visible;

    @Inject(at = @At(value = "HEAD"), method = "addPlayer")
    private void musictriggers_addPlayer(ServerPlayer player, CallbackInfo ci) {
        if(this.visible)
            ServerChannels.addBossBarTracking(player.getUUID(),(ServerBossEvent)(Object)this);
    }

    @Inject(at = @At(value = "HEAD"), method = "removePlayer")
    private void musictriggers_removePlayer(ServerPlayer player, CallbackInfo ci) {
        ServerChannels.removeBossBarTracking(player.getUUID(),(ServerBossEvent)(Object)this);
    }

}
