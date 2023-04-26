package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class PacketInitChannels {

    private ServerChannels data;

    public PacketInitChannels(FriendlyByteBuf buf) {
        ServerChannels.initializePlayerChannels(buf);
    }

    public PacketInitChannels(ServerChannels channelData) {
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
