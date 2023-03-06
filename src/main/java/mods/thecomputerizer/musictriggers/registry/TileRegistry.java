package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.registry.tiles.MusicRecorderEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class TileRegistry {

    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Constants.MODID);
    public static final RegistryObject<TileEntityType<?>> MUSIC_RECORDER_ENTITY = TILE_ENTITIES.register("music_recorder_entity",
            () -> TileEntityType.Builder.of(MusicRecorderEntity::new,BlockRegistry.MUSIC_RECORDER.get()).build(null));

    public static void registerTiles(IEventBus bus) {
        TILE_ENTITIES.register(bus);
    }
}
