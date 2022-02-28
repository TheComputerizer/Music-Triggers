package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.fromServer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public class InfoFromRaid {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "infofromraid");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String s,boolean b) {
        String send = s+","+b;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(id,(client, handler, buf, responseSender) -> {
            String s = decode(buf);
            fromServer.clientSyncRaid(getDataTriggerName(s), getDataBool(s));
        });
    }

    public static String getDataTriggerName(String s) {
        return stringBreaker(s)[0];
    }
    public static boolean getDataBool(String s) {
        return Boolean.parseBoolean(stringBreaker(s)[1]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
