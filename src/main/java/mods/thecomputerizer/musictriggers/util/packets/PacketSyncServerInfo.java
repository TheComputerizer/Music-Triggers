package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.common.ServerChannelData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketSyncServerInfo {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "packet_sync_server_info");
    private final List<ServerChannelData> serverChannelData = new ArrayList<>();

    public static List<ClientSync> decode(PacketByteBuf buf) {
        int size = buf.readInt();
        List<ClientSync> sync = new ArrayList<>();
        for (int i = 0; i < size; i++) sync.add(new ClientSync(buf));
        return sync;
    }

    public PacketSyncServerInfo(List<ServerChannelData> serverChannelData) {
        this.serverChannelData.addAll(serverChannelData);
    }

    public static PacketByteBuf encode(PacketSyncServerInfo packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(packet.serverChannelData.size());
        for (ServerChannelData channel : packet.serverChannelData) channel.encode(buf);
        return buf;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(id,(client, handler, buf, responseSender) -> {
            for (ClientSync sync : decode(buf)) ChannelManager.syncInfoFromServer(sync);
        });
    }
}
