package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.common.eventsCommon;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class MenuSongs {

    private String s;

    public MenuSongs(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public MenuSongs(String name) {
        this.s = name;
    }

    public static void encode(MenuSongs packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final MenuSongs packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        eventsCommon.recordMenu = packet.getSongsWithUUIDAttached();

        ctx.setPacketHandled(true);
    }

    public HashMap<UUID, List<String>> getSongsWithUUIDAttached() {
        if(s==null) {
            return null;
        }
        String[] broken = stringBreaker(s);
        ArrayList<String> ret = new ArrayList<>();
        for(int i=1;i<broken.length;i++) {
            ret.add(broken[i]);
        }
        HashMap<UUID, List<String>> builtMap = new HashMap<>();
        builtMap.put(UUID.fromString(broken[0]),ret);
        return builtMap;
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
