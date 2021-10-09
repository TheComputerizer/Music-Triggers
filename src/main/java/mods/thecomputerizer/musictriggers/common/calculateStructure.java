package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.UUID;

public class calculateStructure {

    public static void calculateAndSend(String struct, BlockPos pos, Integer dimID, UUID uuid) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        WorldServer world = server.getWorld(dimID);
        if(world!=null) {
            if (world.getChunkProvider().isInsideStructure(world, struct, pos)) {
                RegistryHandler.network.sendTo(new packet.packetMessage(Boolean.toString(true)), server.getPlayerList().getPlayerByUUID(uuid));
            } else {
                RegistryHandler.network.sendTo(new packet.packetMessage(Boolean.toString(false)), server.getPlayerList().getPlayerByUUID(uuid));
            }
        }
    }
}
