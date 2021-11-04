package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.util.packets.InfoFromBiome;
import mods.thecomputerizer.musictriggers.util.packets.InfoFromStructure;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Objects;
import java.util.UUID;

public class calculateFeatures {

    public static void calculateStructAndSend(String struct, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerWorld world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                for (Structure<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
                    if(Objects.requireNonNull(structureFeature.getRegistryName()).toString().replace("minecraft:", "").matches(struct)) {
                        if(world.structureFeatureManager().getStructureAt(pos,true,structureFeature.getStructure()).isValid()) {
                            PacketHandler.sendTo(new InfoFromStructure(true,struct), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                        } else {
                            PacketHandler.sendTo(new InfoFromStructure(false,struct), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                        }
                    }
                }
            }
        }
    }

    public static void calculateBiomeAndSend(String biome, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerWorld world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                if (Objects.requireNonNull(world.getBiome(pos).getRegistryName()).toString().contains(biome)) {
                    PacketHandler.sendTo(new InfoFromBiome(true,biome), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                } else {
                    PacketHandler.sendTo(new InfoFromBiome(false,biome), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                }
            }
        }
    }
}
