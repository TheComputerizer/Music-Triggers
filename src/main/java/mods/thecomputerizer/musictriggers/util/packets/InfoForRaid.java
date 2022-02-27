package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class InfoForRaid {
    private String s;

    public InfoForRaid(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoForRaid(String s, int i, BlockPos p, UUID u) {
        this.s = s+","+i+","+p.asLong()+","+u.toString();
    }

    public static void encode(InfoForRaid packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoForRaid packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.calculateRaidAndSend(packet.getDataTriggerName(), packet.getDataWave(), packet.getDataBlockPos(), packet.getDataUUID());

        ctx.setPacketHandled(true);
    }

    public String getDataTriggerName() {
        if(s==null) {
            return null;
        }
        return stringBreaker(s)[0];
    }
    public int getDataWave() {
        return Integer.parseInt(stringBreaker(s)[1]);
    }
    public BlockPos getDataBlockPos() {
        return BlockPos.of(Long.parseLong(stringBreaker(s)[2]));
    }
    public UUID getDataUUID() {
        return UUID.fromString(stringBreaker(s)[3]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
