package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.client.EventsClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class PacketReceiveCommand {

    private final String identifier;

    public PacketReceiveCommand(FriendlyByteBuf buf) {
        this.identifier = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public PacketReceiveCommand(String identifier) {
        this.identifier = identifier;
    }

    public static void encode(PacketReceiveCommand packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.identifier, StandardCharsets.UTF_8);
    }

    public static void handle(final PacketReceiveCommand packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });
        if(packet.getIdentifier()!=null) EventsClient.commandMap.put(packet.getIdentifier(), true);
        ctx.setPacketHandled(true);
    }

    public String getIdentifier() {
        return identifier;
    }
}
