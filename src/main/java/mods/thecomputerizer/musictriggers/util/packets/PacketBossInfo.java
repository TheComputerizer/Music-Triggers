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

public class PacketBossInfo implements IPacket {
    private static final Identifier id = new Identifier(MusicTriggers.MODID, "packet_boss_info");
    private final String s;

    private PacketBossInfo(PacketByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public PacketBossInfo(String name, float health) {
        this.s = name+","+health;
    }

    public PacketByteBuf encode() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(this.s, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            PacketBossInfo packet = new PacketBossInfo(buf);
            CalculateFeatures.bossInfo.put(packet.getBossName(), packet.getDataHealth());
            EventsCommon.bossTimer = 40;
        });
    }

    private String getBossName() {
        if(this.s==null) return null;
        return stringBreaker(this.s,",")[0];
    }

    private float getDataHealth() {
        return Float.parseFloat(stringBreaker(this.s,",")[1]);
    }

    @Override
    public Identifier getID() {
        return id;
    }
}