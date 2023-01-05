package mods.thecomputerizer.musictriggers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;

@Mixin(value = BossHealthOverlay.class)
public interface BossBarHudAccessor {

    @Accessor
    Map<UUID, LerpingBossEvent> getEvents();
}
