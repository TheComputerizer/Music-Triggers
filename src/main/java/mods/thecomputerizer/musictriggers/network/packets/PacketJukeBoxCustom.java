package mods.thecomputerizer.musictriggers.network.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class PacketJukeBoxCustom implements IMessageHandler<PacketJukeBoxCustom.Message, IMessage> {

    @Override
    public IMessage onMessage(Message message, MessageContext ctx) {
        ChannelManager.playCustomJukeboxSong(message.start, message.channel, message.id, message.pos);
        return null;
    }

    public static class Message implements IMessage {
        private BlockPos pos;
        private boolean start;
        private String channel;
        private String id;

        public Message() {
        }

        public Message(BlockPos pos, String channel, String id) {
            this.start = pos!=null;
            this.pos = pos;
            this.channel = channel;
            this.id = id;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.start = buf.readBoolean();
            this.channel = (String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
            this.id = (String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
            if(this.start) this.pos = BlockPos.fromLong(buf.readLong());
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeBoolean(this.start);
            buf.writeInt(this.channel.length());
            buf.writeCharSequence(this.channel, StandardCharsets.UTF_8);
            buf.writeInt(this.id.length());
            buf.writeCharSequence(this.id, StandardCharsets.UTF_8);
            if(this.pos!=null) buf.writeLong(this.pos.toLong());
        }
    }
}
