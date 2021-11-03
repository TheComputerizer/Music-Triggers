package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class MusicTriggersBlocks {

    public static final MusicTriggersBlocks INSTANCE = new MusicTriggersBlocks();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MusicTriggers.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MusicTriggers.MODID);

    public static RegistryObject<Block> MUSIC_RECORDER;

    public void initBlock() {
        MUSIC_RECORDER = BLOCKS.register("music_recorder",() -> new MusicRecorder(AbstractBlock.Properties.of(Material.WOOD,MaterialColor.NONE)));
    }

    public void initItem() {
        BLOCK_ITEMS.register("music_recorder",() -> new BlockItem(MUSIC_RECORDER.get(),new Item.Properties().tab(ItemGroup.TAB_MISC).rarity(Rarity.EPIC).stacksTo(1)));
    }
}
