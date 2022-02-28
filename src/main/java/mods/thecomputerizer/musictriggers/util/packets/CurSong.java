package mods.thecomputerizer.musictriggers.util.packets;


import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

public class CurSong {

    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "cursong");
    public static HashMap<UUID,String> curSong = new HashMap<>();

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String s, UUID u) {
        String send = s+","+u.toString();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            String s = decode(buf);
            curSong.put(getDataUUID(s),getSongName(s));
        });
    }

    public static String getSongName(String s) {
        return stringBreaker(s)[0];
    }

    public static UUID getDataUUID(String s) {
        return UUID.fromString(stringBreaker(s)[1]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}

