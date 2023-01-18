package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MusicTriggersBlocks {

    public static final Block MUSIC_RECORDER = makeBlock("music_recorder", MusicRecorder::new, block -> block.setCreativeTab(CreativeTabs.MISC));

    private static Block makeBlock(final String name, final Supplier<Block> constructor, final Consumer<Block> config) {
        final Block block = constructor.get();
        config.accept(block);
        block.setRegistryName(Constants.MODID + "." + name);
        block.setRegistryName(Constants.MODID, name);
        return block;
    }
}
