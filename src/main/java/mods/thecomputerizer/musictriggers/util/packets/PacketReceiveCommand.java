package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.nio.charset.StandardCharsets;

public class PacketReceiveCommand implements IPacket {
    private static final ResourceLocation id = new ResourceLocation(Constants.MODID, "packet_receive_command");
    private final String identifier;
    private PacketReceiveCommand(FriendlyByteBuf buf) {
        this.identifier = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public PacketReceiveCommand(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(this.identifier, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, responseSender) -> {
            PacketReceiveCommand packet = new PacketReceiveCommand(buf);
            if (packet.identifier != null) EventsClient.commandMap.put(packet.identifier, true);
        });
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }
}
