package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class BlockRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MODID);
    public static final RegistryObject<Block> MUSIC_RECORDER = BLOCKS.register("music_recorder",
            () -> new MusicRecorder(AbstractBlock.Properties.of(Material.WOOD).strength(2F).sound(SoundType.WOOD)));

    public static void registerBlocks(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
