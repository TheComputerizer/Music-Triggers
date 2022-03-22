package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.common.eventsCommon;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class packetBossInfo implements IMessageHandler<packetBossInfo.packetBossInfoMessage, IMessage> {

    @Override
    public IMessage onMessage(packetBossInfo.packetBossInfoMessage message, MessageContext ctx)
    {
        if(message.getBossName()==null) {
            return null;
        }
        calculateFeatures.bossInfo.put(message.getBossName(),message.getDataHealth());
        eventsCommon.bossTimer = 40;
        return null;
    }

    public static class packetBossInfoMessage implements IMessage {
        String s;

        public packetBossInfoMessage() {}

        public packetBossInfoMessage(String name, float health)
        {
            this.s = name+","+health;
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
        public String getBossName() {
            if(s==null) {
                return null;
            }
            return stringBreaker(s)[0];
        }
        public float getDataHealth() {
            return Float.parseFloat(stringBreaker(s)[1]);
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
