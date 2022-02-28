package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class InfoForHome {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "infoforhome");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String trigger, BlockPos p, UUID u, String range) {
        String send = trigger+","+p.asLong()+","+u.toString()+","+range;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            String s = decode(buf);
            calculateFeatures.calculateHomeAndSend(getDataTrigger(s), getDataBlockPos(s), getDataUUID(s), getDataRange(s));
        });
    }

    public static String getDataTrigger(String s) {
        return stringBreaker(s)[0];
    }
    public static BlockPos getDataBlockPos(String s) {
        return BlockPos.fromLong(Long.parseLong(stringBreaker(s)[1]));
    }
    public static UUID getDataUUID(String s) {
        return UUID.fromString(stringBreaker(s)[2]);
    }
    public static int getDataRange(String s) {
        return Integer.parseInt(stringBreaker(s)[3]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
