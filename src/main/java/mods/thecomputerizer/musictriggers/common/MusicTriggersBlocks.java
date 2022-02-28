package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class MusicTriggersBlocks {

    public static final Identifier MUSIC_RECORDER_ID = new Identifier(MusicTriggersCommon.MODID,"music_recorder");
    public static final Block MUSIC_RECORDER = new MusicRecorder(AbstractBlock.Settings.of(Material.WOOD).strength(5f));
    public static final BlockItem MUSIC_RECORDER_ITEM = new BlockItem(MUSIC_RECORDER, new Item.Settings().rarity(Rarity.EPIC).fireproof().group(ItemGroup.MISC));

    public static void initBlocks() {
        Registry.register(Registry.BLOCK,MUSIC_RECORDER_ID,MUSIC_RECORDER);
        Registry.register(Registry.ITEM,MUSIC_RECORDER_ID,MUSIC_RECORDER_ITEM);
    }
}
