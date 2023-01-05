package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.util.packets.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.server.level.ServerPlayer;


public class PacketHandler {

    public static void registerReceivers() {
        PacketBossInfo.register();
        PacketQueryServerInfo.register();
        PacketJukeBoxCustom.register();
        if(FabricLoaderImpl.INSTANCE.getEnvironmentType()!=EnvType.SERVER) {
            PacketSyncServerInfo.register();
            PacketReceiveCommand.register();
        }
    }

    public static void sendTo(IPacket packet, ServerPlayer player) {
        ServerPlayNetworking.send(player,packet.getID(),packet.encode());
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(IPacket packet) {
        ClientPlayNetworking.send(packet.getID(),packet.encode());
    }
}
