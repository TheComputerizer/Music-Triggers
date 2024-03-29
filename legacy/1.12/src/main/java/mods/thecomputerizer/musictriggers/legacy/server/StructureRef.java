package mods.thecomputerizer.musictriggers.legacy.server;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class StructureRef {

    private static final Set<StructureRef> REFS = addVanillaRefs();

    private static void addVanillaRef(Set<StructureRef> refs, String id, String name,
                                      @Nullable BiFunction<WorldServer,BlockPos,Boolean> posCheck) {
        refs.add(new StructureRef(new ResourceLocation(id),name,posCheck));
    }

    private static Set<StructureRef> addVanillaRefs() {
        Set<StructureRef> refs = new HashSet<>();
        addVanillaRef(refs,"end_city","EndCity",null);
        addVanillaRef(refs,"fortress","Fortress",null);
        addVanillaRef(refs,"mansion","Mansion",null);
        addVanillaRef(refs,"mineshaft","Mineshaft",null);
        addVanillaRef(refs,"monument","Monument",null);
        addVanillaRef(refs,"stronghold","Stronghold",null);
        addVanillaRef(refs,"swamp_hut","SwampHut",(world,pos) -> {
            if(world.getChunkProvider().isInsideStructure(world,"Temple",pos)) {
                IChunkGenerator generator = world.getChunkProvider().chunkGenerator;
                if(generator instanceof ChunkGeneratorOverworld)
                    return ((ChunkGeneratorOverworld)generator).scatteredFeatureGenerator.isSwampHut(pos);
            }
            return false;
        });
        addVanillaRef(refs,"temple","Temple",null);
        addVanillaRef(refs,"village","Village",null);
        return refs;
    }

    public static @Nullable StructureRef getStructureAt(WorldServer world, BlockPos pos) {
        for(StructureRef ref : REFS)
            if(ref.isPosInside(world,pos)) return ref;
        return null;
    }

    public static void register(ResourceLocation id, String name) {
        REFS.add(new StructureRef(id,name,null));
    }

    public static void register(ResourceLocation id, String name, BiFunction<WorldServer,BlockPos,Boolean> posCheck) {
        REFS.add(new StructureRef(id,name,posCheck));
    }

    @Getter private final ResourceLocation id;
    @Getter private final String name;
    private final BiFunction<WorldServer,BlockPos,Boolean> posCheck;

    private StructureRef(ResourceLocation id, String name, BiFunction<WorldServer,BlockPos,Boolean> posCheck) {
        this.id = id;
        this.name = name;
        this.posCheck = posCheck;
    }

    public boolean isPosInside(WorldServer world, BlockPos pos) {
        return Objects.nonNull(this.posCheck) ? this.posCheck.apply(world,pos) :
                world.getChunkProvider().isInsideStructure(world,this.name,pos);
    }

    @Override
    public boolean equals(Object other) {
        if(super.equals(other)) return true;
        if(other instanceof StructureRef) {
            StructureRef otherRef = (StructureRef)other;
            return this.id.equals(otherRef.id) && ((Objects.isNull(this.posCheck) &&
                    Objects.isNull(otherRef.posCheck)) || this.posCheck.equals(otherRef.posCheck));
        }
        return false;
    }
}