package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.client.model.obj.MaterialLibrary;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MusicTriggersBlocks {

    public static final MusicTriggersBlocks INSTANCE = new MusicTriggersBlocks();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MusicTriggers.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MusicTriggers.MODID);

    public static RegistryObject<Block> MUSIC_RECORDER;

    public void initBlock() {
        MUSIC_RECORDER = BLOCKS.register("music_recorder",() -> new MusicRecorder(Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.0F, 6.0F).requiresCorrectToolForDrops()));
    }

    public void initItem() {
        BLOCK_ITEMS.register("music_recorder",() -> new BlockItem(MUSIC_RECORDER.get(),new Item.Properties().tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC).stacksTo(1)));
    }
}
