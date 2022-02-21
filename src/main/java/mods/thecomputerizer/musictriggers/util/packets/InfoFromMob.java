package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.client.fromServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class InfoFromMob {
    private String s;

    public InfoFromMob(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoFromMob(String s,boolean b) {
        this.s = s+","+b;
    }

    public static void encode(InfoFromMob packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoFromMob packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        fromServer.clientSyncMob(packet.getMobName(), packet.getPassBoolean());

        ctx.setPacketHandled(true);
    }

    public String getMobName() {
        if(s==null) {
            return null;
        }
        return stringBreaker(s)[0];
    }
    public Boolean getPassBoolean() {
        return Boolean.parseBoolean(stringBreaker(s)[1]);
    }


    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
