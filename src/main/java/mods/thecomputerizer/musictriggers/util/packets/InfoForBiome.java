package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class InfoForBiome {
    private String s;

    public InfoForBiome(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoForBiome(String s, BlockPos p, UUID u, String ct, String rt, String t, String c) {
        this.s = s+","+p.asLong()+","+u.toString()+","+ct+","+rt+","+t+","+c;
    }

    public static void encode(InfoForBiome packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoForBiome packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.calculateBiomeAndSend(packet.getDataStruct(), packet.getDataBlockPos(), packet.getDataUUID(),
                packet.getDataCategory(), packet.getDataRainType(), packet.getDataTemperature(), packet.getDataCold());

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
    public String getDataCategory() {
        return stringBreaker(s)[3];
    }
    public String getDataRainType() {
        return stringBreaker(s)[4];
    }
    public float getDataTemperature() {
        return Float.parseFloat(stringBreaker(s)[5]);
    }
    public boolean getDataCold() {
        return Boolean.parseBoolean(stringBreaker(s)[6]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
