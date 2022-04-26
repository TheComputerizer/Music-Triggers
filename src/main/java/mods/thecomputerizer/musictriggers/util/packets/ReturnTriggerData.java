package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.FromServer;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public class ReturnTriggerData {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "packet_return_trigger_data");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String triggerData) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(triggerData, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            CalculateFeatures.curServer = server;
            FromServer.clientSync(decode(buf));
        });
    }
}
