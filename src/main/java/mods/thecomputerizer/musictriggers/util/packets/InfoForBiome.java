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

public class InfoForBiome {
    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "infoforbiome");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String trigger, String s, BlockPos p, UUID u, String ct, String rt, String t, String c) {
        String send = trigger+","+s+","+p.asLong()+","+u.toString()+","+ct+","+rt+","+t+","+c;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            String s = decode(buf);
            calculateFeatures.calculateBiomeAndSend(getDataTrigger(s), getDataStruct(s), getDataBlockPos(s), getDataUUID(s),
                    getDataCategory(s), getDataRainType(s), getDataTemperature(s), getDataCold(s));
        });
    }

    public static String getDataTrigger(String s) {
        return stringBreaker(s)[0];
    }
    public static String getDataStruct(String s) {
        return stringBreaker(s)[1];
    }
    public static BlockPos getDataBlockPos(String s) {
        return BlockPos.fromLong(Long.parseLong(stringBreaker(s)[2]));
    }
    public static UUID getDataUUID(String s) {
        return UUID.fromString(stringBreaker(s)[3]);
    }
    public static String getDataCategory(String s) {
        return stringBreaker(s)[4];
    }
    public static String getDataRainType(String s) {
        return stringBreaker(s)[5];
    }
    public static float getDataTemperature(String s) {
        return Float.parseFloat(stringBreaker(s)[6]);
    }
    public static boolean getDataCold(String s) {
        return Boolean.parseBoolean(stringBreaker(s)[7]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
