package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.server.ServerData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class PacketInitChannels {

    private ServerData data;

    public PacketInitChannels(PacketBuffer buf) {
        ServerData.initializePlayerChannels(buf);
    }

    public PacketInitChannels(ServerData channelData) {
        this.data = channelData;
    }

    public static void encode(PacketInitChannels packet, PacketBuffer buf) {
        packet.data.encodeForServer(buf);
    }

    public static void handle(final PacketInitChannels packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });
        ctx.setPacketHandled(true);
    }
}
