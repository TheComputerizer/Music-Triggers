package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = ServerBossEvent.class)
public class MixinServerBossEvent {

    @Inject(at = @At("RETURN"), method = "<init>")
    private void ServerBossEvent(Component name, BossEvent.BossBarColor event, BossEvent.BossBarOverlay overlay, CallbackInfo callback) {
        calculateFeatures.bossInfo.add((net.minecraft.server.level.ServerBossEvent)(Object)this);
        MusicTriggers.logger.info("Boss info mixin");
    }
}
