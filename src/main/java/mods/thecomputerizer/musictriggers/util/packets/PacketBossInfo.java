package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.mutable.MutableInt;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class PacketBossInfo implements IPacket {
    private static final ResourceLocation id = new ResourceLocation(Constants.MODID, "packet_boss_info");
    private final String name;
    private final float health;
    private final String playerUUID;

    private PacketBossInfo(FriendlyByteBuf buf) {
        this.name = ((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8));
        this.health = buf.readFloat();
        this.playerUUID = ((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8));
    }

    public PacketBossInfo(String name, float health, String playerUUID) {
        this.name = name;
        this.health = health;
        this.playerUUID = playerUUID;
    }

    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(this.name.length());
        buf.writeCharSequence(this.name, StandardCharsets.UTF_8);
        buf.writeFloat(this.health);
        buf.writeInt(this.playerUUID.length());
        buf.writeCharSequence(this.playerUUID, StandardCharsets.UTF_8);
        return buf;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(id,(server, player, handler, buf, sender) -> {
            PacketBossInfo packet = new PacketBossInfo(buf);
            CalculateFeatures.perPlayerBossInfo.putIfAbsent(player.getUUID(), new HashMap<>());
            CalculateFeatures.perPlayerBossInfo.get(player.getUUID()).put(packet.name, packet.health);
            EventsCommon.bossTimers.put(player.getUUID(), new MutableInt(40));
        });
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }
}