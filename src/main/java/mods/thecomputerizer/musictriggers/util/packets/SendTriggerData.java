package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

import static mods.thecomputerizer.musictriggers.MusicTriggersCommon.stringBreaker;

public class SendTriggerData {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "packet_send_trigger_data");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String triggerData, UUID playerUUID) {
        String send = triggerData+"/"+playerUUID;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            CalculateFeatures.curServer = server;
            String s = decode(buf);
            CalculateFeatures.calculateServerTriggers(stringBreaker(getTriggerData(s),"#"), getPlayerUUID(s));
        });
    }

    public static String getTriggerData(String s) {
        return stringBreaker(s,"/")[0];
    }

    public static UUID getPlayerUUID(String s) {
        return UUID.fromString(s.substring(s.lastIndexOf("/") + 1));
    }
}
