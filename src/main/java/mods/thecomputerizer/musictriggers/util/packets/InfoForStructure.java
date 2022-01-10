package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class InfoForStructure {
    private String s;

    public InfoForStructure(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoForStructure(String s, BlockPos p, UUID u) {
        this.s = s+","+p.asLong()+","+u.toString();
    }

    public static void encode(InfoForStructure packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoForStructure packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.calculateStructAndSend(packet.getDataStruct(), packet.getDataBlockPos(), packet.getDataUUID());

        ctx.setPacketHandled(true);
    }

    public String getDataStruct() {
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
