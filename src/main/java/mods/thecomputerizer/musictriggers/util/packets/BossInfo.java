package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class BossInfo {

    private String s;

    public BossInfo(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public BossInfo(String name, float health) {
        this.s = name+","+health;
    }

    public static void encode(BossInfo packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final BossInfo packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.bossInfo.put(packet.getBossName(), packet.getDataHealth());

        ctx.setPacketHandled(true);
    }

    public String getBossName() {
        if(s==null) {
            return null;
        }
        return stringBreaker(s)[0];
    }

    public float getDataHealth() {
        return Float.parseFloat(stringBreaker(s)[1]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}