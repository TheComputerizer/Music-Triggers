package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

import java.util.Objects;

public class MessageSeekSong<CTX> extends MessageAPI<CTX> {
    
    private final String channel;
    private final long seconds;
    
    public MessageSeekSong(String channel, long seconds) {
        this.channel = Objects.nonNull(channel) && !channel.isEmpty() ? channel : "-";
        this.seconds = seconds;
    }
    
    public MessageSeekSong(ByteBuf buf) {
        this.channel = NetworkHelper.readString(buf);
        this.seconds = buf.readLong();
    }
    
    @Override public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,this.channel);
        buf.writeLong(this.seconds);
    }
    
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.nonNull(helper)) helper.seek(this.channel,this.seconds);
        return null;
    }
}
