package mods.thecomputerizer.musictriggers.util;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.common.calculateStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class packet implements IMessageHandler<packet.packetMessage, IMessage> {

    @Override
    public IMessage onMessage(packetMessage message, MessageContext ctx)
    {
        if(message.getDataStruct()==null) {
            return null;
        }
        calculateStructure.calculateAndSend(message.getDataStruct(), message.getDataBlockPos(), message.getDataInt(), message.getDataUUID());
        return null;
    }

    public static class packetMessage implements IMessage {
        String s;

        public packetMessage() {}

        public packetMessage(String s, BlockPos p, int d, UUID u)
        {
            this.s = s+","+p.toLong()+","+d+","+u.toString();
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
        public String getDataStruct() {
            if(s==null) {
                return null;
            }
            return stringBreaker(s)[0];
        }
        public BlockPos getDataBlockPos() {
            return BlockPos.fromLong(Long.parseLong(stringBreaker(s)[1]));
        }
        public Integer getDataInt() {
            return Integer.parseInt(stringBreaker(s)[2]);
        }
        public UUID getDataUUID() {
            return UUID.fromString(stringBreaker(s)[3]);
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
