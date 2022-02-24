package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

public class packetCurSong implements IMessageHandler<packetCurSong.packetCurSongMessage, IMessage> {
    public static HashMap<UUID,String> curSong = new HashMap<>();

    @Override
    public IMessage onMessage(packetCurSong.packetCurSongMessage message, MessageContext ctx)
    {
        if(message.getSongName()==null) {
            return null;
        }
        curSong.put(message.getDataUUID(),message.getSongName());
        return null;
    }

    public static class packetCurSongMessage implements IMessage {
        String s;

        public packetCurSongMessage() {}

        public packetCurSongMessage(String s,UUID u)
        {
            this.s = s+","+u.toString();
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
        public String getSongName() {
            if(s==null) {
                return null;
            }
            return stringBreaker(s)[0];
        }
        public UUID getDataUUID() {
            return UUID.fromString(stringBreaker(s)[1]);
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}

