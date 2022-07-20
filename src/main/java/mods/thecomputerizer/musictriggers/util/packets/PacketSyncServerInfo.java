package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketSyncServerInfo {

    private final List<ServerChannelData> serverChannelData = new ArrayList<>();
    private final List<ClientSync> clientReturnInfo = new ArrayList<>();

    public PacketSyncServerInfo(PacketBuffer buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) this.clientReturnInfo.add(new ClientSync(buf));
    }

    public PacketSyncServerInfo(List<ServerChannelData> serverChannelData) {
        this.serverChannelData.addAll(serverChannelData);
    }

    public static void encode(PacketSyncServerInfo packet, PacketBuffer buf) {
        buf.writeInt(packet.serverChannelData.size());
        for (ServerChannelData channel : packet.serverChannelData) channel.encode(buf);
    }

    public static void handle(final PacketSyncServerInfo packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });
        for (ClientSync sync : packet.clientReturnInfo) ChannelManager.syncInfoFromServer(sync);
        ctx.setPacketHandled(true);
    }
}
