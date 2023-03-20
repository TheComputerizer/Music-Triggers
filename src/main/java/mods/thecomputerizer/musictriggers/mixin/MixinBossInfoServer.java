package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.server.ServerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.BossInfoServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossInfoServer.class)
public class MixinBossInfoServer {

    @Shadow
    private boolean visible;

    @Inject(at = @At(value = "HEAD"), method = "addPlayer")
    private void musictriggers_addPlayer(EntityPlayerMP player, CallbackInfo ci) {
        if(this.visible)
            ServerData.addBossBarTracking(player.getUniqueID(),(BossInfoServer)(Object)this);
    }

    @Inject(at = @At(value = "HEAD"), method = "removePlayer")
    private void musictriggers_removePlayer(EntityPlayerMP player, CallbackInfo ci) {
        ServerData.removeBossBarTracking(player.getUniqueID(),(BossInfoServer)(Object)this);
    }
}
