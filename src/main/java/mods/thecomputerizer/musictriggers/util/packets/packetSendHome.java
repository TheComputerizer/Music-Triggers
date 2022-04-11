package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class packetSendHome implements IMessageHandler<packetSendHome.packetSendHomeMessage, IMessage> {

    @Override
    public IMessage onMessage(packetSendHome.packetSendHomeMessage message, MessageContext ctx)
    {
        if(message.getDataTriggerName()==null) return null;

        calculateFeatures.calculateHomeAndSend(message.getDataTriggerName(), message.getDataInt(), message.getDataUUID());

        return null;
    }

    public static class packetSendHomeMessage implements IMessage {
        String s;

        public packetSendHomeMessage() {}

        public packetSendHomeMessage(String trigger, int d, UUID u)
        {
            this.s = trigger+","+d+","+u.toString();
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeCharSequence(s, StandardCharsets.UTF_8);
        }
        public String getDataTriggerName() {
            if(s==null) {
                return null;
            }
            return stringBreaker(s)[0];
        }
        public Integer getDataInt() {
            return Integer.parseInt(stringBreaker(s)[1]);
        }
        public UUID getDataUUID() {
            return UUID.fromString(stringBreaker(s)[2]);
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
