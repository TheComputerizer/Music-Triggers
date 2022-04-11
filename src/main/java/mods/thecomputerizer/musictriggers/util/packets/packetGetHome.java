package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.fromServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class packetGetHome implements IMessageHandler<packetGetHome.packetGetHomeMessage, IMessage> {

    @Override
    public IMessage onMessage(packetGetHome.packetGetHomeMessage message, MessageContext ctx)
    {
        if(message.triggerName()==null) {
            return null;
        }
        fromServer.clientSyncHome(message.getPassBoolean());
        return null;
    }

    public static class packetGetHomeMessage implements IMessage {
        String s;

        public packetGetHomeMessage() {}

        public packetGetHomeMessage(String s, boolean b)
        {
            this.s = s+","+b;
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
        public String triggerName() {
            if(s==null) {
                return null;
            }
            return stringBreaker(s)[0];
        }
        public Boolean getPassBoolean() {
            return Boolean.parseBoolean(stringBreaker(s)[1]);
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
