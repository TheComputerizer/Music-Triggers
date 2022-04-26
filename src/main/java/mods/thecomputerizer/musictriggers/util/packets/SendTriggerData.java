package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class SendTriggerData {
    private String s;

    public SendTriggerData(FriendlyByteBuf buf) {
        this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
    }

    public SendTriggerData(String triggerData, UUID playerUUID) {
        this.s = triggerData+"/"+playerUUID;
    }

    public static void encode(SendTriggerData packet, FriendlyByteBuf buf) {
        buf.writeCharSequence(packet.s, StandardCharsets.UTF_8);
    }

    public static void handle(final SendTriggerData packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });
        CalculateFeatures.calculateServerTriggers(stringBreaker(packet.getTriggerData(),"#"), packet.getPlayerUUID());
        ctx.setPacketHandled(true);
    }

    public String getTriggerData() {
        return stringBreaker(this.s,"/")[0];
    }

    public UUID getPlayerUUID() {
        return UUID.fromString(s.substring(s.lastIndexOf("/") + 1));
    }
}
