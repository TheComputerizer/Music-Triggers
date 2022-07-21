package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PacketQueryServerInfo {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "packet_query_server_info");
    private final List<Channel> clientChannels = new ArrayList<>();

    public static List<ServerChannelData> decode(PacketByteBuf buf, MinecraftServer server) {
        int size = buf.readInt();
        List<ServerChannelData> data = new ArrayList<>();
        for (int i = 0; i < size; i++) data.add(ServerChannelData.decode(buf, server));
        return data;
    }

    public PacketQueryServerInfo(List<Channel> clientChannels) {
        this.clientChannels.addAll(clientChannels);
    }

    public static PacketByteBuf encode(PacketQueryServerInfo packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(packet.clientChannels.size());
        for (Channel channel : packet.clientChannels) channel.encode(buf);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            List<ServerChannelData> packetData = decode(buf, server);
            if (!packetData.isEmpty()) {
                List<ServerChannelData> serverChannelData = new ArrayList<>();
                EventsCommon.currentSongs.put(player.getUuid(), new ArrayList<>());
                for (ServerChannelData data : packetData) {
                    serverChannelData.add(CalculateFeatures.calculateServerTriggers(data,server));
                }
                PacketHandler.sendTo(PacketSyncServerInfo.id,PacketSyncServerInfo.encode(new PacketSyncServerInfo(serverChannelData)),player);
            }
        });
    }
}
