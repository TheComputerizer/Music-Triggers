package mods.thecomputerizer.musictriggers.registry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class Tabs {

    public static final CreativeTabs MUSIC_TRIGGERS_TAB = new CreativeTabs("musictriggers") {
        @SideOnly(Side.CLIENT)
        @Nonnull
        public ItemStack createIcon() {
            return new ItemStack(ItemRegistry.MUSIC_RECORDER);
        }
    };
}
