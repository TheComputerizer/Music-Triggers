package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.packetToClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.UUID;

public class calculateStructure {

    public static void calculateAndSend(String struct, BlockPos pos, Integer dimID, UUID uuid) {
        MusicTriggers.logger.info("Beginning of computation: "+dimID+"\n");
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer world = server.getWorld(dimID);
        if(world!=null) {
            if (world.getChunkProvider().isInsideStructure(world, struct, pos)) {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(true +","+struct), server.getPlayerList().getPlayerByUUID(uuid));
            } else {
                RegistryHandler.network.sendTo(new packetToClient.packetToClientMessage(false +","+struct), server.getPlayerList().getPlayerByUUID(uuid));
            }
        }
    }
}
