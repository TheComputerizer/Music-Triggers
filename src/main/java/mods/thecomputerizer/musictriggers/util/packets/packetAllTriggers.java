package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.util.calculateFeatures;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class packetAllTriggers implements IMessageHandler<packetAllTriggers.packetAllTriggersMessage, IMessage> {

    @Override
    public IMessage onMessage(packetAllTriggers.packetAllTriggersMessage message, MessageContext ctx)
    {
        if(message.getTriggers()==null || message.getTriggers().isEmpty()) {
            return null;
        }
        calculateFeatures.allTriggers = message.getTriggers();
        return null;
    }

    public static class packetAllTriggersMessage implements IMessage {
        String s;

        public packetAllTriggersMessage() {}

        public packetAllTriggersMessage(String name)
        {
            this.s = name;
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
        public List<String> getTriggers() {
            if(s==null) {
                return null;
            }
            return new ArrayList<>(Arrays.asList(stringBreaker(s)));
        }
        public float getDataHealth() {
            return Float.parseFloat(stringBreaker(s)[1]);
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
