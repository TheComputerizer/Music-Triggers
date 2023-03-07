package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.registry.tiles.MusicRecorderEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class TileRegistry {

    public static final Map<ResourceLocation, BlockEntityType<? extends BlockEntity>> TILE_MAP = new HashMap<>();
    public static final BlockEntityType<MusicRecorderEntity> MUSIC_RECORDER_ENTITY = addTileRegistry("music_recorder_entity",
            () -> FabricBlockEntityTypeBuilder.create(MusicRecorderEntity::new,BlockRegistry.MUSIC_RECORDER).build(null));

    private static <T extends BlockEntity> BlockEntityType<T> addTileRegistry(String id, Supplier<BlockEntityType<T>> supplier) {
        BlockEntityType<T> type = supplier.get();
        TILE_MAP.put(new ResourceLocation(Constants.MODID,id),type);
        return type;
    }

    public static void register() {
        for(Map.Entry<ResourceLocation, BlockEntityType<? extends BlockEntity>> entry : TILE_MAP.entrySet())
            Registry.register(Registry.BLOCK_ENTITY_TYPE,entry.getKey(),entry.getValue());
    }
}
