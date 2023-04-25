package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class PacketInitChannels {

    private ServerChannels data;

    public PacketInitChannels(PacketBuffer buf) {
        ServerChannels.initializePlayerChannels(buf);
    }

    public PacketInitChannels(ServerChannels channelData) {
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
