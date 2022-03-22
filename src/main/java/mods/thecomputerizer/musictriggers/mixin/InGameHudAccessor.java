package mods.thecomputerizer.musictriggers.mixin;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(value = InGameHud.class)
public interface InGameHudAccessor {

    @Accessor
    BossBarHud getBossBarHud();
}
