package mods.thecomputerizer.musictriggers.network.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketInitChannels implements IMessageHandler<PacketInitChannels.Message, IMessage> {

    @Override
    public IMessage onMessage(PacketInitChannels.Message message, MessageContext ctx) {
        return null;
    }

    public static class Message implements IMessage {

        private ServerChannels data;

        public Message() {}

        public Message(ServerChannels channelData) {
            this.data = channelData;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            ServerChannels.initializePlayerChannels(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            this.data.encodeForServer(buf);
        }
    }
}
