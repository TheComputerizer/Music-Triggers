package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public record PacketReceiveCommand(String identifier) {
    public static final Identifier id = new Identifier(MusicTriggers.MODID, "packet_receive_command");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(PacketReceiveCommand packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(packet.identifier, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(id, (client, handler, buf, responseSender) -> {
            String id = decode(buf);
            if (id != null) EventsClient.commandMap.put(id, true);
        });
    }
}
