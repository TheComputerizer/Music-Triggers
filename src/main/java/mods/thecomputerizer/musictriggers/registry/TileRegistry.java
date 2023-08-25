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

public final class TileRegistry {

    public static final Map<ResourceLocation, BlockEntityType<? extends BlockEntity>> TILE_MAP = new HashMap<>();
    public static final BlockEntityType<MusicRecorderEntity> MUSIC_RECORDER_ENTITY = addTileRegistry("music_recorder_entity",
            FabricBlockEntityTypeBuilder.create(MusicRecorderEntity::new,BlockRegistry.MUSIC_RECORDER).build(null));

    @SuppressWarnings("SameParameterValue")
    private static <T extends BlockEntity> BlockEntityType<T> addTileRegistry(String id, BlockEntityType<T> type) {
        TILE_MAP.put(Constants.res(id),type);
        return type;
    }

    public static void register() {
        for(Map.Entry<ResourceLocation, BlockEntityType<? extends BlockEntity>> entry : TILE_MAP.entrySet())
            Registry.register(Registry.BLOCK_ENTITY_TYPE,entry.getKey(),entry.getValue());
    }
}
