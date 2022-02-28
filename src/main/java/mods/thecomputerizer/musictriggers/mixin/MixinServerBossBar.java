package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = ServerBossBar.class)
public class MixinServerBossBar {

    @Inject(at = @At("RETURN"), method = "<init>")
    private void ServerBossBar(Text displayName, BossBar.Color color, BossBar.Style style, CallbackInfo info) {
        calculateFeatures.bossInfo.add((ServerBossBar)(Object)this);
        MusicTriggersCommon.logger.info("Boss info mixin");
    }
}
