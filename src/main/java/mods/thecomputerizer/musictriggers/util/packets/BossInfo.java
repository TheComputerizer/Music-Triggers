package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

import static mods.thecomputerizer.musictriggers.MusicTriggersCommon.stringBreaker;

public class BossInfo {

    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "bossinfo");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String name, float percent) {
        String send = name+","+percent;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(send, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            CalculateFeatures.curServer = server;
            String s = decode(buf);
            CalculateFeatures.bossInfo.put(getDataName(s), getDataPercent(s));
            EventsCommon.bossTimer = 40;
        });
    }

    public static String getDataName(String s) {
        return stringBreaker(s,",")[0];
    }
    public static float getDataPercent(String s) {
        return Float.parseFloat(stringBreaker(s,",")[1]);
    }
}
