package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.network.packets.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NetworkHandler {

    private static final Map<Class<?>,ResourceLocation> ID_MAP = new HashMap<>();

    public static void registerReceivers() {
        registerServerReceiver(PacketInitChannels.class,PacketInitChannels::handle);
        registerServerReceiver(PacketDynamicChannelInfo.class,PacketDynamicChannelInfo::handle);
        registerClientReceiver(PacketJukeBoxCustom.class,PacketJukeBoxCustom::handle);
        registerClientReceiver(PacketReceiveCommand.class,PacketReceiveCommand::handle);
        registerClientReceiver(PacketSyncServerInfo.class,PacketSyncServerInfo::handle);
    }

    private static void registerClientReceiver(Class<? extends IPacket> classType,
                                               Supplier<ClientPlayNetworking.PlayChannelHandler> handler) {
        ResourceLocation id = new ResourceLocation(Constants.MODID,classType.getSimpleName().toLowerCase());
        ClientPlayNetworking.registerGlobalReceiver(id,handler.get());
        ID_MAP.put(classType,id);
    }

    private static void registerServerReceiver(Class<? extends IPacket> classType,
                                               Supplier<ServerPlayNetworking.PlayChannelHandler> handler) {
        ResourceLocation id = new ResourceLocation(Constants.MODID,classType.getSimpleName().toLowerCase());
        ServerPlayNetworking.registerGlobalReceiver(id,handler.get());
        ID_MAP.put(classType,id);
    }

    public static void sendTo(IPacket packet, ServerPlayer player) {
        Class<? extends IPacket> classType = packet.getClass();
        if(ID_MAP.containsKey(classType))
            ServerPlayNetworking.send(player,ID_MAP.get(classType),packet.encode());
        else Constants.MAIN_LOG.error("Tried to send unknown packet type of class {}",classType.getName());
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(IPacket packet) {
        Class<? extends IPacket> classType = packet.getClass();
        if(ID_MAP.containsKey(classType))
            ClientPlayNetworking.send(ID_MAP.get(classType),packet.encode());
        else Constants.MAIN_LOG.error("Tried to send unknown packet type of class {}",classType.getName());
    }
}
