package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class InfoForMob {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "infoformob");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String trigger, UUID u, String n, String r, String t, String tp, String h, String hp, String v, String vi, String num, int time, int to, String nbt) {
        String send = trigger+","+u.toString()+","+n+","+r+","+t+","+tp+","+h+","+hp+","+v+","+vi+","+num+","+time+","+to+","+nbt;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            calculateFeatures.curServer = server;
            String s = decode(buf);
            calculateFeatures.calculateMobAndSend(getDataTriggerName(s), getDataUUID(s), getMobName(s), getDetectionRange(s),
                    getTargettingBoolean(s), getHordeTargettingPercentage(s), getHealth(s), getHealthTargettingPercentage(s),
                    getVictoryBoolean(s), getVictoryID(s), getMobNumber(s), getTime(s), getTimeOut(s), getNBT(s));
        });
    }

    public static String getDataTriggerName(String s) {
        return stringBreaker(s)[0];
    }
    public static UUID getDataUUID(String s) {
        return UUID.fromString(stringBreaker(s)[1]);
    }
    public static String getMobName(String s) {
        return stringBreaker(s)[2];
    }
    public static Integer getDetectionRange(String s) {
        return Integer.parseInt(stringBreaker(s)[3]);
    }
    public static Boolean getTargettingBoolean(String s) {
        return Boolean.parseBoolean(stringBreaker(s)[4]);
    }
    public static Integer getHordeTargettingPercentage(String s) {
        return Integer.parseInt(stringBreaker(s)[5]);
    }
    public static Integer getHealth(String s) {
        return Integer.parseInt(stringBreaker(s)[6]);
    }
    public static Integer getHealthTargettingPercentage(String s) {
        return Integer.parseInt(stringBreaker(s)[7]);
    }
    public static Boolean getVictoryBoolean(String s) {
        return Boolean.parseBoolean(stringBreaker(s)[8]);
    }
    public static Integer getVictoryID(String s) {
        return Integer.parseInt(stringBreaker(s)[9]);
    }
    public static Integer getMobNumber(String s) {
        return Integer.parseInt(stringBreaker(s)[10]);
    }
    public static Integer getTime(String s) {
        return Integer.parseInt(stringBreaker(s)[11]);
    }
    public static Integer getTimeOut(String s) {
        return Integer.parseInt(stringBreaker(s)[12]);
    }
    public static String getNBT(String s) {
        return stringBreaker(s)[13];
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
