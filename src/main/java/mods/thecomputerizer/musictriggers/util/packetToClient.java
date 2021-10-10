package mods.thecomputerizer.musictriggers.util;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.fromServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class packetToClient implements IMessageHandler<packetToClient.packetToClientMessage, IMessage> {

    @Override
    public IMessage onMessage(packetToClient.packetToClientMessage message, MessageContext ctx)
    {
        fromServer.clientSync(message.getDataBoolean());
        return null;
    }

    public static class packetToClientMessage implements IMessage {
        String s;

        public packetToClientMessage() {}

        public packetToClientMessage(String s)
        {
            this.s = s;
            MusicTriggers.logger.info("Boolean achieved: "+s+"\n");
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
            MusicTriggers.logger.info("From Bytes (To Client): "+s+"\n");
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if(s!=null) {
                buf.writeCharSequence(s, StandardCharsets.UTF_8);
            }
        }
        public boolean getDataBoolean() {
            return s.matches("true");
        }
    }
}
