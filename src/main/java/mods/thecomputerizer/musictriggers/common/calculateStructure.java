package mods.thecomputerizer.musictriggers.common;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class calculateStructure {

    public static void calculateAndSend(String struct, BlockPos pos, Integer dimID, UUID uuid) {
        /*
        MinecraftServer server = FMLEnvironment.dist.isDedicatedServer();
        WorldServer world = server.getWorld(dimID);
        if(world!=null) {
            if (world.getChunkProvider().isInsideStructure(world, struct, pos)) {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(true +","+struct), server.getPlayerList().getPlayerByUUID(uuid));
            } else {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(false +","+struct), server.getPlayerList().getPlayerByUUID(uuid));
            }
        }

         */
    }
}
