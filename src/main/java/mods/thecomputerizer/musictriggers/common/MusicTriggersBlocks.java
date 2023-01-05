package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class MusicTriggersBlocks {

    public static final ResourceLocation MUSIC_RECORDER_ID = new ResourceLocation(Constants.MODID,"music_recorder");
    public static final Block MUSIC_RECORDER = new MusicRecorder(BlockBehaviour.Properties.of(Material.WOOD).strength(5f));

    public static void initBlocks() {
        Registry.register(Registry.BLOCK,MUSIC_RECORDER_ID,MUSIC_RECORDER);
    }
}
