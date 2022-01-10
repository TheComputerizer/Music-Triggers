package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.util.packets.InfoFromBiome;
import mods.thecomputerizer.musictriggers.util.packets.InfoFromStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Objects;
import java.util.UUID;

public class calculateFeatures {

    public static void calculateStructAndSend(String struct, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerLevel world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                boolean good = false;
                String curStruct = null;
                for (StructureFeature<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
                    if(world.structureFeatureManager().getStructureAt(pos, structureFeature).isValid()) {
                        if(structureFeature.getRegistryName()!=null) {
                            curStruct = structureFeature.getRegistryName().toString().replace("minecraft:", "");
                            if(curStruct.matches(struct)) {
                                good = true;
                            }
                        }
                    }
                }
                PacketHandler.sendTo(new InfoFromStructure(good,struct,curStruct), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
            }
        }
    }

    public static void calculateBiomeAndSend(String biome, BlockPos pos, UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayer(uuid)!=null) {
            ServerLevel world = server.getLevel(Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)).level.dimension());
            if (world != null) {
                String curBiome = Objects.requireNonNull(world.getBiome(pos).getRegistryName()).toString();
                if (curBiome.contains(biome)) {
                    PacketHandler.sendTo(new InfoFromBiome(true,biome,curBiome), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                } else {
                    PacketHandler.sendTo(new InfoFromBiome(false,biome,curBiome), Objects.requireNonNull(server.getPlayerList().getPlayer(uuid)));
                }
            }
        }
    }
}
