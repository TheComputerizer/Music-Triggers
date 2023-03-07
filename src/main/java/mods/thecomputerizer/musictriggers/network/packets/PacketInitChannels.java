package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.server.ServerData;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;

@SuppressWarnings("unused")
public class PacketInitChannels implements IPacket {

    private ServerData data;

    private PacketInitChannels(MinecraftServer server, FriendlyByteBuf buf) {
        ServerData.initializePlayerChannels(server,buf);
    }

    public PacketInitChannels(ServerData channelData) {
        this.data = channelData;
    }

    @Override
    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        this.data.encodeForServer(buf);
        return buf;
    }

    public static ServerPlayNetworking.PlayChannelHandler handle() {
        return (server, player, handler, buf, sender) -> new PacketInitChannels(server,buf);
    }
}
