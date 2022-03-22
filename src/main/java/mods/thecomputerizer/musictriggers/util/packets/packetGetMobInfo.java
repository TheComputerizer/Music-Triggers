package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.fromServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class packetGetMobInfo  implements IMessageHandler<packetGetMobInfo.packetGetMobInfoMessage, IMessage> {

    @Override
    public IMessage onMessage(packetGetMobInfo.packetGetMobInfoMessage message, MessageContext ctx)
    {
        if(message.getMobName()==null) {
            return null;
        }
        fromServer.clientSyncMob(message.getMobName(), message.getPassBoolean(),
                message.getVictoryID(), message.getVictoryBoolean());
        return null;
    }

    public static class packetGetMobInfoMessage implements IMessage {
        String s;

        public packetGetMobInfoMessage() {}

        public packetGetMobInfoMessage(String s, boolean b, int i, boolean v)
        {
            this.s = s+","+b+","+i+","+v;
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
        public String getMobName() {
            if(s==null) {
                return null;
            }
            return stringBreaker(s)[0];
        }
        public Boolean getPassBoolean() {
            return Boolean.parseBoolean(stringBreaker(s)[1]);
        }

        public int getVictoryID() {
            return Integer.parseInt(stringBreaker(s)[2]);
        }
        public Boolean getVictoryBoolean() {
            return Boolean.parseBoolean(stringBreaker(s)[3]);
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
