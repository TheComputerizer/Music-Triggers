package mods.thecomputerizer.musictriggers.util.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class MobTarget {
    private String s;

    public MobTarget(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public MobTarget(String s, UUID u) {
        this.s = s+","+u.toString();
    }

    public static void encode(MobTarget packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final MobTarget packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });


        ctx.setPacketHandled(true);
    }

    public String getSongName() {
        if(s==null) {
            return null;
        }
        return stringBreaker(s)[0];
    }

    public UUID getDataUUID() {
        return UUID.fromString(stringBreaker(s)[1]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
