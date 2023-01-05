package mods.thecomputerizer.musictriggers.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(value = Gui.class)
public interface InGameHudAccessor {

    @Accessor
    BossHealthOverlay getBossOverlay();
}
