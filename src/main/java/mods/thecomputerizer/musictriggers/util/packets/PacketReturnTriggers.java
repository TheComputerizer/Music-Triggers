package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.FromServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class PacketReturnTriggers implements IMessageHandler<PacketReturnTriggers.PacketReturnTriggersMessage, IMessage> {

    @Override
    public IMessage onMessage(PacketReturnTriggers.PacketReturnTriggersMessage message, MessageContext ctx) {
        if(message.getTriggerData()!=null) FromServer.clientSync(message.getTriggerData());
        return null;
    }

    public static class PacketReturnTriggersMessage implements IMessage {
        String s;

        public PacketReturnTriggersMessage() {
        }

        public PacketReturnTriggersMessage(String triggerData) {
            this.s = triggerData;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeCharSequence(this.s, StandardCharsets.UTF_8);
        }

        public String getTriggerData() {
            return s;
        }
    }
}
