package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class BlockRegistry {

    public static final Map<ResourceLocation, Block> BLOCK_MAP = new HashMap<>();
    public static final Block MUSIC_RECORDER = addBlockRegistry("music_recorder",
            () -> new MusicRecorder(BlockBehaviour.Properties.of(Material.WOOD).strength(2F).sound(SoundType.WOOD)));

    private static Block addBlockRegistry(String id, Supplier<Block> supplier) {
        Block block = supplier.get();
        BLOCK_MAP.put(new ResourceLocation(Constants.MODID,id),block);
        return block;
    }

    public static void register() {
        for(Map.Entry<ResourceLocation, Block> entry : BLOCK_MAP.entrySet())
            Registry.register(Registry.BLOCK,entry.getKey(),entry.getValue());
    }
}
