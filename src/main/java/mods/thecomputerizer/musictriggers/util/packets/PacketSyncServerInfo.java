package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

public class PacketSyncServerInfo implements IPacket {
    private static final ResourceLocation id = new ResourceLocation(Constants.MODID, "packet_sync_server_info");
    private final List<ServerChannelData> serverChannelData = new ArrayList<>();
    private final List<ClientSync> sync = new ArrayList<>();

    private PacketSyncServerInfo(FriendlyByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) this.sync.add(new ClientSync(buf));
    }

    public PacketSyncServerInfo(List<ServerChannelData> serverChannelData) {
        this.serverChannelData.addAll(serverChannelData);
    }

    @Override
    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(this.serverChannelData.size());
        for (ServerChannelData channel : this.serverChannelData) channel.encode(buf);
        return buf;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(id,(client, handler, buf, responseSender) -> {
            PacketSyncServerInfo packet = new PacketSyncServerInfo(buf);
            for (ClientSync sync : packet.sync) ChannelManager.syncInfoFromServer(sync);
        });
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }
}
