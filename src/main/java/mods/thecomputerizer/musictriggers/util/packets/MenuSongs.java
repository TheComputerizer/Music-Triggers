package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static mods.thecomputerizer.musictriggers.MusicTriggersCommon.stringBreaker;

public class MenuSongs {

    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "menusongs");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String name) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(name, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            CalculateFeatures.curServer = server;
            String s = decode(buf);
            EventsCommon.recordMenu = getSongsWithUUIDAttached(s);
        });
    }

    public static HashMap<UUID, List<String>> getSongsWithUUIDAttached(String s) {
        if(s==null) return null;
        String[] broken = stringBreaker(s,",");
        ArrayList<String> ret = new ArrayList<>(Arrays.asList(broken).subList(1, broken.length));
        HashMap<UUID, List<String>> builtMap = new HashMap<>();
        builtMap.put(UUID.fromString(broken[0]),ret);
        return builtMap;
    }
}
