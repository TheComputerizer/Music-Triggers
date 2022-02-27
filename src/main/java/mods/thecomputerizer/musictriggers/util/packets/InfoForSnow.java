package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class InfoForSnow {
    private String s;

    public InfoForSnow(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoForSnow(String trigger, BlockPos p, UUID u) {
        this.s = trigger+","+p.asLong()+","+u.toString();
    }

    public static void encode(InfoForSnow packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoForSnow packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.calculateSnowAndSend(packet.getDataTrigger(), packet.getDataBlockPos(), packet.getDataUUID());

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

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
