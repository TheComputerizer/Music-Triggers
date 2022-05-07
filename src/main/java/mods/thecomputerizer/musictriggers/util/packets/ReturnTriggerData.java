package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.client.FromServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class ReturnTriggerData {
    private final String s;

    public ReturnTriggerData(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public ReturnTriggerData(String returnData) {
        this.s = returnData;
    }

    public static void encode(ReturnTriggerData packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final ReturnTriggerData packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        FromServer.clientSync(packet.getReturnData());

        ctx.setPacketHandled(true);
    }

    public String getReturnData() {
        return this.s;
    }
}
