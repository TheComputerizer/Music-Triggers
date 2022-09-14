package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggers;
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
import java.util.HashMap;
import java.util.List;

public class PacketQueryServerInfo implements IPacket {
    private static final Identifier id = new Identifier(MusicTriggers.MODID, "packet_query_server_info");
    private final List<Channel> clientChannels = new ArrayList<>();
    private final List<ServerChannelData> data = new ArrayList<>();

    public PacketQueryServerInfo(PacketByteBuf buf, MinecraftServer server) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) this.data.add(ServerChannelData.decode(buf, server));
    }

    public PacketQueryServerInfo(List<Channel> clientChannels) {
        this.clientChannels.addAll(clientChannels);
    }

    @Override
    public PacketByteBuf encode() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(this.clientChannels.size());
        for (Channel channel : this.clientChannels) channel.encode(buf);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            PacketQueryServerInfo packet = new PacketQueryServerInfo(buf,server);
            if (!packet.data.isEmpty()) {
                List<ServerChannelData> serverChannelData = new ArrayList<>();
                EventsCommon.currentChannelSongs.put(player.getUuid(),new HashMap<>());
                EventsCommon.activeTriggerList.put(player.getUuid(),new HashMap<>());
                for (ServerChannelData data : packet.data)
                    serverChannelData.add(CalculateFeatures.calculateServerTriggers(data,server));
                PacketHandler.sendTo(new PacketSyncServerInfo(serverChannelData),player);
            }
        });
    }

    @Override
    public Identifier getID() {
        return id;
    }
}
