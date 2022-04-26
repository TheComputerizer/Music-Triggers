package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static mods.thecomputerizer.musictriggers.MusicTriggersCommon.stringBreaker;

public class AllTriggers {

    public static final Identifier id = new Identifier(MusicTriggersCommon.MODID, "alltriggers");

    public static String decode(PacketByteBuf buf) {
        return ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public static PacketByteBuf encode(String triggers) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCharSequence(triggers, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            CalculateFeatures.curServer = server;
            String s = decode(buf);
            CalculateFeatures.allTriggers = getTriggers(s);
        });
    }

    public static List<String> getTriggers(String s) {
        return new ArrayList<>(Arrays.asList(stringBreaker(s,",")));
    }
}
