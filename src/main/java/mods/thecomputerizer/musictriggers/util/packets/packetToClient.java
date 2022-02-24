package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.fromServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class packetToClient implements IMessageHandler<packetToClient.packetToClientMessage, IMessage> {

    @Override
    public IMessage onMessage(packetToClient.packetToClientMessage message, MessageContext ctx)
    {
        fromServer.clientSync(message.getDataBoolean(),message.getDataString());
        return null;
    }

    public static class packetToClientMessage implements IMessage {
        String s;

        public packetToClientMessage() {}

        public packetToClientMessage(String s)
        {
            this.s = s;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if(s!=null) {
                buf.writeCharSequence(s, StandardCharsets.UTF_8);
            }
        }
        public boolean getDataBoolean() {
            return stringBreaker(s)[0].matches("true");
        }
        public String getDataString() {
            return stringBreaker(s)[1];
        }
        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
