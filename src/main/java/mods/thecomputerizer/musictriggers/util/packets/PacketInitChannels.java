package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.ServerData;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketInitChannels implements IMessageHandler<PacketInitChannels.Message, IMessage> {

    @Override
    public IMessage onMessage(PacketInitChannels.Message message, MessageContext ctx) {
        Constants.MAIN_LOG.error("ON MESSAGE SIDE {}",ctx.side);
        return null;
    }

    public static class Message implements IMessage {

        private ServerData data;

        public Message() {}

        public Message(ServerData channelData) {
            Constants.MAIN_LOG.error("SENDING SERVER DATA");
            this.data = channelData;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            Constants.MAIN_LOG.error("DECODING SERVER DATA");
            ServerData.initializePlayerChannels(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            Constants.MAIN_LOG.error("ENCODING SERVER DATA");
            this.data.encodeForServer(buf);
        }
    }
}
