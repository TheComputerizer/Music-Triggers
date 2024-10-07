package mods.thecomputerizer.musictriggers.api.data.channel;

import io.netty.buffer.ByteBuf;

public interface ChannelSyncable {
    
    void encode(ByteBuf buf);
}
