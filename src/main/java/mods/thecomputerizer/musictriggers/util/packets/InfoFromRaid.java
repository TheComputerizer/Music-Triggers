package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.client.fromServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class InfoFromRaid {
    private String s;

    public InfoFromRaid(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoFromRaid(String s,boolean b) {
        this.s = s+","+b;
    }

    public static void encode(InfoFromRaid packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoFromRaid packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        fromServer.clientSyncRaid(packet.getDataTriggerName(), packet.getDataBool());

        ctx.setPacketHandled(true);
    }

    public String getDataTriggerName() {
        return stringBreaker(s)[0];
    }
    public boolean getDataBool() {
        return Boolean.parseBoolean(stringBreaker(s)[1]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
