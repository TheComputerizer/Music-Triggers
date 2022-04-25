package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.CalculateFeatures;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class PacketSendTriggers implements IMessageHandler<PacketSendTriggers.PacketSendTriggersMessage, IMessage> {

    @Override
    public IMessage onMessage(PacketSendTriggers.PacketSendTriggersMessage message, MessageContext ctx) {
        if(message.getTriggerData()!=null) {
            CalculateFeatures.calculateServerTriggers(stringBreaker(message.getTriggerData(), "#"), message.getPlayerUUID());
        }
        return null;
    }

    public static class PacketSendTriggersMessage implements IMessage {
        String s;

        public PacketSendTriggersMessage() {
        }

        public PacketSendTriggersMessage(String triggerData, UUID playerUUID) {
            this.s = triggerData+"/"+playerUUID;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeCharSequence(s, StandardCharsets.UTF_8);
        }

        public String getTriggerData() {
            return stringBreaker(this.s,"/")[0];
        }

        public UUID getPlayerUUID() {
            return UUID.fromString(s.substring(s.lastIndexOf("/") + 1));
        }
    }
}
