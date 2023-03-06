package mods.thecomputerizer.musictriggers.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public final class Tabs {

    public static final ItemGroup MUSIC_TRIGGERS_TAB = new ItemGroup("musictriggers") {
        @OnlyIn(Dist.CLIENT)
        @Nonnull
        public ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.MUSIC_RECORDER.get());
        }
    };
}
