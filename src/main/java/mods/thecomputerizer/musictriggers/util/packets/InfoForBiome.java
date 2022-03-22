package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class InfoForBiome {
    private String s;

    public InfoForBiome(PacketBuffer buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public InfoForBiome(String trigger, String s, BlockPos p, UUID u, String ct, String rt, String t, String c, String downfall, String toggle) {
        this.s = trigger+","+s+","+p.asLong()+","+u.toString()+","+ct+","+rt+","+t+","+c+","+downfall+","+toggle;
    }

    public static void encode(InfoForBiome packet, PacketBuffer buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final InfoForBiome packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.calculateBiomeAndSend(packet.getDataTrigger(), packet.getDataStruct(), packet.getDataBlockPos(), packet.getDataUUID(),
                packet.getDataCategory(), packet.getDataRainType(), packet.getDataTemperature(), packet.getDataCold(), packet.getDataDownfall(),
                packet.getDataToggle());

        ctx.setPacketHandled(true);
    }

    public String getDataTrigger() {
        if(s==null) {
            return null;
        }
        return stringBreaker(s)[0];
    }

    public String getDataStruct() {
        return stringBreaker(s)[1];
    }

    public BlockPos getDataBlockPos() {
        return BlockPos.of(Long.parseLong(stringBreaker(s)[2]));
    }
    public UUID getDataUUID() {
        return UUID.fromString(stringBreaker(s)[3]);
    }
    public String getDataCategory() {
        return stringBreaker(s)[4];
    }
    public String getDataRainType() {
        return stringBreaker(s)[5];
    }
    public float getDataTemperature() {
        return Float.parseFloat(stringBreaker(s)[6]);
    }
    public boolean getDataCold() {
        return Boolean.parseBoolean(stringBreaker(s)[7]);
    }
    public float getDataDownfall() {
        return Float.parseFloat(stringBreaker(s)[8]);
    }
    public boolean getDataToggle() {
        return Boolean.parseBoolean(stringBreaker(s)[9]);
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
