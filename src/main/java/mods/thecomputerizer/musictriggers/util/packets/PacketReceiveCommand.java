package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public class PacketReceiveCommand implements IPacket {
    private static final Identifier id = new Identifier(MusicTriggers.MODID, "packet_receive_command");
    private final String identifier;
    private PacketReceiveCommand(PacketByteBuf buf) {
        this.identifier = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public PacketReceiveCommand(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public PacketByteBuf encode() {
        PacketByteBuf buf = PacketByteBufs.create();
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
    public Identifier getID() {
        return id;
    }
}
