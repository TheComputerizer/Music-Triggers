package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.function.Supplier;

public class PacketQueryServerInfo {

    private final List<Channel> clientChannels = new ArrayList<>();
    private final List<ServerChannelData> serverChannelData = new ArrayList<>();
    private UUID playerUUID;

    public PacketQueryServerInfo(FriendlyByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) this.serverChannelData.add(ServerChannelData.decode(buf));
        this.playerUUID = this.serverChannelData.get(0).getPlayerUUID();
    }

    public PacketQueryServerInfo(List<Channel> clientChannels) {
        this.clientChannels.addAll(clientChannels);
    }

    public static void encode(PacketQueryServerInfo packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.clientChannels.size());
        for (Channel channel : packet.clientChannels) channel.encode(buf);
    }

    public static void handle(final PacketQueryServerInfo packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });
        if (!packet.serverChannelData.isEmpty()) {
            List<ServerChannelData> serverChannelData = new ArrayList<>();
            EventsCommon.currentChannelSongs.put(packet.playerUUID(),new HashMap<>());
            EventsCommon.activeTriggerList.put(packet.playerUUID(),new HashMap<>());
            for (ServerChannelData data : packet.serverChannelData)
                serverChannelData.add(CalculateFeatures.calculateServerTriggers(data));
            if(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(packet.playerUUID())!=null)
                PacketHandler.sendTo(new PacketSyncServerInfo(serverChannelData), Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(packet.playerUUID())));
        }
        ctx.setPacketHandled(true);
    }

    public List<ServerChannelData> serverData() {
        return this.serverChannelData;
    }

    public UUID playerUUID() {
        return this.playerUUID;
    }
}
