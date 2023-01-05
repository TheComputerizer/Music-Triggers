package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.common.EventsCommon;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.mutable.MutableInt;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PacketBossInfo implements IMessageHandler<PacketBossInfo.PacketBossInfoMessage, IMessage> {

    @SuppressWarnings("ConstantConditions")
    @Override
    public IMessage onMessage(PacketBossInfoMessage message, MessageContext ctx) {
        if(Objects.isNull(message.name)) return null;
        UUID uuid = UUID.fromString(message.playerUUID);
        EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);
        if(Objects.nonNull(player)) {
            CalculateFeatures.perPlayerBossInfo.putIfAbsent(uuid, new HashMap<>());
            CalculateFeatures.perPlayerBossInfo.get(uuid).put(message.name, message.health);
            EventsCommon.bossTimers.put(uuid, new MutableInt(40));
        }
        return null;
    }

    public static class PacketBossInfoMessage implements IMessage {
        private String name;
        private float health;
        private String playerUUID;

        public PacketBossInfoMessage() {}

        public PacketBossInfoMessage(String name, float health, String playerUUID) {
            this.name = name;
            this.health = health;
            this.playerUUID = playerUUID;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.name = ((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8));
            this.health = buf.readFloat();
            this.playerUUID = ((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8));
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(this.name.length());
            buf.writeCharSequence(this.name, StandardCharsets.UTF_8);
            buf.writeFloat(this.health);
            buf.writeInt(this.playerUUID.length());
            buf.writeCharSequence(this.playerUUID, StandardCharsets.UTF_8);
        }
    }
}
