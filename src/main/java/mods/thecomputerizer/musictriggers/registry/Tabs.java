package mods.thecomputerizer.musictriggers.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public final class Tabs {

    public static final CreativeModeTab MUSIC_TRIGGERS_TAB = new CreativeModeTab("musictriggers") {
        @OnlyIn(Dist.CLIENT)
        @Nonnull
        public ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.MUSIC_RECORDER.get());
        }
    };
}
