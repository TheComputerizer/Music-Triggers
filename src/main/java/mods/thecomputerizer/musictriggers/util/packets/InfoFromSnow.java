package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.client.fromServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class InfoFromSnow {
    private String s;

    public InfoFromSnow(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoFromSnow(boolean b,String s) {
        this.s = b +","+s;
    }

    public static void encode(InfoFromSnow packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoFromSnow packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        fromServer.clientSyncSnow(packet.getDataBool(), packet.getDataTrigger());

        ctx.setPacketHandled(true);
    }

    public String getDataTrigger() {
        return stringBreaker(s)[1];
    }
    public boolean getDataBool() {
        return Boolean.parseBoolean(stringBreaker(s)[0]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
