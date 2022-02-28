package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.client.fromServer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public class InfoFromMob {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "infofrommob");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String s,boolean b,int i,boolean v) {
        String send = s+","+b+","+i+","+v;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(id,(client, handler, buf, responseSender) -> {
            String s = decode(buf);
            fromServer.clientSyncMob(getMobName(s), getPassBoolean(s), getVictoryID(s), getVictoryBoolean(s));
        });
    }

    public static String getMobName(String s) {
        return stringBreaker(s)[0];
    }
    public static Boolean getPassBoolean(String s) {
        return Boolean.parseBoolean(stringBreaker(s)[1]);
    }
    public static int getVictoryID(String s) {
        return Integer.parseInt(stringBreaker(s)[2]);
    }
    public static Boolean getVictoryBoolean(String s) {
        return Boolean.parseBoolean(stringBreaker(s)[3]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
