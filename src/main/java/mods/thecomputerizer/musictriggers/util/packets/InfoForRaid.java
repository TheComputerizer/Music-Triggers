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

public class InfoForRaid {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "infoforraid");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String s, int i, BlockPos p, UUID u) {
        String send = s+","+i+","+p.asLong()+","+u.toString();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            calculateFeatures.curServer = server;
            String s = decode(buf);
            calculateFeatures.calculateRaidAndSend(getDataTriggerName(s), getDataWave(s), getDataBlockPos(s), getDataUUID(s));
        });
    }

    public static String getDataTriggerName(String s) {
        if(s==null) {
            return null;
        }
        return stringBreaker(s)[0];
    }
    public static int getDataWave(String s) {
        return Integer.parseInt(stringBreaker(s)[1]);
    }
    public static BlockPos getDataBlockPos(String s) {
        return BlockPos.fromLong(Long.parseLong(stringBreaker(s)[2]));
    }
    public static UUID getDataUUID(String s) {
        return UUID.fromString(stringBreaker(s)[3]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
