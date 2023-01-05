package mods.thecomputerizer.musictriggers.util.packets;

import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.mutable.MutableInt;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketBossInfo {

    private final String name;
    private final float health;
    private final String playerUUID;

    public PacketBossInfo(PacketBuffer buf) {
        this.name = ((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8));
        this.health = buf.readFloat();
        this.playerUUID = ((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8));
    }

    public PacketBossInfo(String name, float health, String playerUUID) {
        this.name = name;
        this.health = health;
        this.playerUUID = playerUUID;
    }

    public static void encode(PacketBossInfo packet, PacketBuffer buf) {
        buf.writeInt(packet.name.length());
        buf.writeCharSequence(packet.name, StandardCharsets.UTF_8);
        buf.writeFloat(packet.health);
        buf.writeInt(packet.playerUUID.length());
        buf.writeCharSequence(packet.playerUUID, StandardCharsets.UTF_8);
    }

    public static void handle(final PacketBossInfo packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {});
        UUID uuid = UUID.fromString(packet.playerUUID);
        ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
        if(Objects.nonNull(player)) {
            CalculateFeatures.perPlayerBossInfo.putIfAbsent(uuid, new HashMap<>());
            CalculateFeatures.perPlayerBossInfo.get(uuid).put(packet.name, packet.health);
            EventsCommon.bossTimers.put(uuid, new MutableInt(40));
        }
        ctx.setPacketHandled(true);
    }
}