package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class Tabs {

    public static final CreativeModeTab MUSIC_TRIGGERS_TAB = FabricItemGroupBuilder.build(
            new ResourceLocation(Constants.MODID,"musictriggers"),() -> new ItemStack(ItemRegistry.MUSIC_RECORDER));
}
