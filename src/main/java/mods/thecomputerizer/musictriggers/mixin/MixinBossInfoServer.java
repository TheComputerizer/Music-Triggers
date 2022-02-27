package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = BossInfoServer.class)
public class MixinBossInfoServer {

    @Inject(at = @At("RETURN"), method = "<init>")
    private void BossInfoServer(ITextComponent nameIn, BossInfo.Color colorIn, BossInfo.Overlay overlayIn, CallbackInfo callback) {
        calculateFeatures.bossInfo.add((BossInfoServer)(Object)this);
        MusicTriggers.logger.info("Boss info mixin");
    }
}
