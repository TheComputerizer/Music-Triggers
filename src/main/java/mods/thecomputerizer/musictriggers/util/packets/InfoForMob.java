package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class InfoForMob {
    private String s;

    public InfoForMob(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoForMob(String trigger, UUID u, String n, String r, String t, String tp, String h, String hp, String v, String vi, String i, String num, int time, int to, String nbt) {
        this.s = trigger+","+u.toString()+","+n+","+r+","+t+","+tp+","+h+","+hp+","+v+","+vi+","+i+","+num+","+time+","+to+","+nbt;
    }

    public static void encode(InfoForMob packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoForMob packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.calculateMobAndSend(packet.getDataTriggerName(), packet.getDataUUID(), packet.getMobName(), packet.getDetectionRange(),
                packet.getTargettingBoolean(), packet.getHordeTargettingPercentage(), packet.getHealth(),
                packet.getHealthTargettingPercentage(), packet.getVictoryBoolean(), packet.getVictoryID(), packet.getInfernalID(),
                packet.getMobNumber(), packet.getTime(), packet.getTimeOut(), packet.getNBT());

        ctx.setPacketHandled(true);
    }

    public String getDataTriggerName() {
        if(s==null) {
            return null;
        }
        return stringBreaker(s)[0];
    }
    public UUID getDataUUID() {
        return UUID.fromString(stringBreaker(s)[1]);
    }
    public String getMobName() {
        return stringBreaker(s)[2];
    }
    public Integer getDetectionRange() {
        return Integer.parseInt(stringBreaker(s)[3]);
    }
    public Boolean getTargettingBoolean() {
        return Boolean.parseBoolean(stringBreaker(s)[4]);
    }
    public Integer getHordeTargettingPercentage() {
        return Integer.parseInt(stringBreaker(s)[5]);
    }
    public Integer getHealth() {
        return Integer.parseInt(stringBreaker(s)[6]);
    }
    public Integer getHealthTargettingPercentage() {
        return Integer.parseInt(stringBreaker(s)[7]);
    }
    public Boolean getVictoryBoolean() {
        return Boolean.parseBoolean(stringBreaker(s)[8]);
    }
    public Integer getVictoryID() {
        return Integer.parseInt(stringBreaker(s)[9]);
    }
    public String getInfernalID() {
        return stringBreaker(s)[10];
    }
    public Integer getMobNumber() {
        return Integer.parseInt(stringBreaker(s)[11]);
    }
    public Integer getTime() {
        return Integer.parseInt(stringBreaker(s)[12]);
    }
    public Integer getTimeOut() {
        return Integer.parseInt(stringBreaker(s)[13]);
    }
    public String getNBT() {
        return stringBreaker(s)[14];
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
