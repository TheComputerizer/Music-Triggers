package mods.thecomputerizer.musictriggers.util.packets;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class CurSong {
    public static HashMap<UUID,String> curSong = new HashMap<>();
    private final String s;

    public CurSong(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public CurSong(String s, UUID u) {
        this.s = s+","+u.toString();
    }

    public static void encode(CurSong packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final CurSong packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        curSong.put(packet.getDataUUID(),packet.getSongName());

        ctx.setPacketHandled(true);
    }

    public String getSongName() {
        if(s==null) return null;
        return stringBreaker(s,",")[0];
    }

    public UUID getDataUUID() {
        return UUID.fromString(stringBreaker(s,",")[1]);
    }
}

