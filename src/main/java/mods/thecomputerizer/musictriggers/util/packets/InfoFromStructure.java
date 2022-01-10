package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.client.fromServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class InfoFromStructure {
    private String s;

    public InfoFromStructure(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoFromStructure(boolean b,String s,String d) {
        this.s = b+","+s+","+d;
    }

    public static void encode(InfoFromStructure packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoFromStructure packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        fromServer.clientSyncStruct(packet.getDataBool(), packet.getDataStruct(), packet.getDataCurStruct());

        ctx.setPacketHandled(true);
    }

    public String getDataCurStruct() {
        return stringBreaker(s)[2];
    }
    public String getDataStruct() {
        return stringBreaker(s)[1];
    }
    public boolean getDataBool() {
        return Boolean.parseBoolean(stringBreaker(s)[0]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
