package mods.thecomputerizer.musictriggers.mixin;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = BossBarHud.class)
public interface BossBarHudAccessor {

    @Accessor
    Map<UUID, ClientBossBar> getBossBars();
}
