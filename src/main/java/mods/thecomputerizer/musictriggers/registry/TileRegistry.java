package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.registry.tiles.MusicRecorderEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TileRegistry {

    private static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MODID);
    public static final RegistryObject<BlockEntityType<MusicRecorderEntity>> MUSIC_RECORDER_ENTITY = TILE_ENTITIES.register("music_recorder_entity",
            () -> BlockEntityType.Builder.of(MusicRecorderEntity::new,BlockRegistry.MUSIC_RECORDER.get()).build(null));

    public static void registerTiles(IEventBus bus) {
        TILE_ENTITIES.register(bus);
    }
}
