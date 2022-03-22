package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class AllTriggers {

    private String s;

    public AllTriggers(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public AllTriggers(String name) {
        this.s = name;
    }

    public static void encode(AllTriggers packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final AllTriggers packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });

        calculateFeatures.allTriggers = packet.getTriggers();

        ctx.setPacketHandled(true);
    }

    public List<String> getTriggers() {
        if(s==null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(stringBreaker(s)));
    }

    public static String[] stringBreaker(String s) {
        return s.split(",");
    }
}
