package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class InfoForHome {
    private String s;

    public InfoForHome(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoForHome(String trigger, BlockPos p, UUID u, String range) {
        this.s = trigger+","+p.asLong()+","+u.toString()+","+range;
    }

    public static void encode(InfoForHome packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoForHome packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.calculateHomeAndSend(packet.getDataTrigger(), packet.getDataBlockPos(), packet.getDataUUID(), packet.getDataRange());

        ctx.setPacketHandled(true);
    }

    public String getDataTrigger() {
        if(s==null) {
            return null;
        }
        return stringBreaker(s)[0];
    }
    public BlockPos getDataBlockPos() {
        return BlockPos.of(Long.parseLong(stringBreaker(s)[1]));
    }
    public UUID getDataUUID() {
        return UUID.fromString(stringBreaker(s)[2]);
    }
    public int getDataRange() {
        return Integer.parseInt(stringBreaker(s)[3]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
