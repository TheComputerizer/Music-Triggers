package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.server.ServerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class PacketInitChannels {

    private ServerData data;

    public PacketInitChannels(FriendlyByteBuf buf) {
        ServerData.initializePlayerChannels(buf);
    }

    public PacketInitChannels(ServerData channelData) {
        this.data = channelData;
    }

    public static void encode(PacketInitChannels packet, FriendlyByteBuf buf) {
        packet.data.encodeForServer(buf);
    }

    public static void handle(final PacketInitChannels packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });
        ctx.setPacketHandled(true);
    }
}
