package mods.thecomputerizer.musictriggers.util;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.fromServer;
import mods.thecomputerizer.musictriggers.common.calculateStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class packet implements IMessageHandler<packet.packetMessage, IMessage> {

    @Override
    public IMessage onMessage(packetMessage message, MessageContext ctx)
    {
        if (ctx.side == Side.CLIENT) {
            fromServer.clientSync(message.getDataBoolean());
        }
        else if (ctx.side == Side.SERVER) {
            calculateStructure.calculateAndSend(message.getDataStruct(), message.getDataBlockPos(), message.getDataInt(), message.getDataUUID());
        }
        return null;
    }

    public static class packetMessage implements IMessage {
        String s;
        BlockPos p;
        int d = 3;
        UUID u;
        String b;

        public packetMessage() {}

        public packetMessage(String s, BlockPos p, int d, UUID u)
        {
            this.s = s;
            this.p = p;
            this.d = d;
            this.u = u;
        }

        public packetMessage(String b) {
            this.b = b;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            if(s!=null) {
                this.s = ((String) buf.readCharSequence(s.length(), StandardCharsets.UTF_8));
            }
            if(p!=null) {
                this.p = BlockPos.fromLong(buf.readLong());
            }
            if(d==3) {
                this.d = buf.readInt();
            }
            if(u!=null) {
                try {
                    this.u = new TypeConverters.UuidConverter().convert(((String) buf.readCharSequence(b.length(), StandardCharsets.UTF_8)));
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if(b!=null) {
                this.b = ((String) buf.readCharSequence(b.length(), StandardCharsets.UTF_8));
            }
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if(s!=null) {
                buf.writeCharSequence(s, StandardCharsets.UTF_8);
            }
            if(p!=null) {
                buf.writeLong(p.toLong());
            }
            if(d==3) {
                buf.writeInt(d);
            }
            if(u!=null) {
                buf.writeCharSequence(u.toString(), StandardCharsets.UTF_8);
            }
            if(b!=null) {
                buf.writeCharSequence(b, StandardCharsets.UTF_8);
            }
        }
        public String getDataStruct() {
            return s;
        }
        public BlockPos getDataBlockPos() {
            return p;
        }
        public Integer getDataInt() {
            return d;
        }
        public UUID getDataUUID() {
            return u;
        }
        public boolean getDataBoolean() {
            return b.matches("true");
        }
    }
}
