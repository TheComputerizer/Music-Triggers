package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class PacketBossInfo {
    public static final Identifier id = new Identifier(MusicTriggers.MODID, "packet_boss_info");
    private final String s;

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public PacketBossInfo(String name, float health) {
        this.s = name+","+health;
    }

    public static PacketByteBuf encode(PacketBossInfo packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            String s = decode(buf);
            CalculateFeatures.bossInfo.put(getBossName(s), getDataHealth(s));
            EventsCommon.bossTimer = 40;
        });
    }

    public static String getBossName(String s) {
        if(s==null) return null;
        return stringBreaker(s,",")[0];
    }

    public static float getDataHealth(String s) {
        return Float.parseFloat(stringBreaker(s,",")[1]);
    }
}