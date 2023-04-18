package mods.thecomputerizer.musictriggers.network.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketDynamicChannelInfo implements IMessageHandler<PacketDynamicChannelInfo.Message, IMessage> {

    @Override
    public IMessage onMessage(Message message, MessageContext ctx) {
        return null;
    }

    public static class Message implements IMessage {
        private final List<Channel> clientChannels = new ArrayList<>();

        public Message() {
        }

        public Message(List<Channel> clientChannels) {
            this.clientChannels.addAll(clientChannels);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            ServerChannels.decodeDynamicInfo(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NetworkUtil.writeString(buf, Minecraft.getMinecraft().player.getUniqueID().toString());
            NetworkUtil.writeGenericList(buf,this.clientChannels,(buf1, channel) -> channel.encodeDynamic(buf1));
        }
    }
}
