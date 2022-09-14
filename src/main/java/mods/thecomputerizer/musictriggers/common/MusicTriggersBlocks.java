package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MusicTriggersBlocks {

    public static final Identifier MUSIC_RECORDER_ID = new Identifier(MusicTriggers.MODID,"music_recorder");
    public static final Block MUSIC_RECORDER = new MusicRecorder(AbstractBlock.Settings.of(Material.WOOD).strength(5f));

    public static void initBlocks() {
        Registry.register(Registry.BLOCK,MUSIC_RECORDER_ID,MUSIC_RECORDER);
    }
}
