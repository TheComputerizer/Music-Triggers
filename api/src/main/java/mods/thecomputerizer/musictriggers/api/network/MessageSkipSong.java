package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

import java.util.Objects;

public class MessageSkipSong<CTX> extends MessageAPI<CTX> {
    
    public MessageSkipSong() {}
    
    public MessageSkipSong(ByteBuf buf) {
        buf.readBoolean();
    }
    
    @Override public void encode(ByteBuf buf) {
        buf.writeBoolean(true);
    }
    
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.nonNull(helper)) helper.forEachChannel(channel -> {
            if(channel.isClientChannel()) channel.stopped();
        });
        return null;
    }
}
