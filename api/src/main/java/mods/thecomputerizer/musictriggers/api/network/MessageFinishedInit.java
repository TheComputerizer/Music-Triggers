package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;

import java.util.Objects;

public class MessageFinishedInit<CTX> extends ChannelHelperMessage<CTX> {
    
    final boolean hasWorldData;
    final CompoundTagAPI<?> worldData;
    
    public MessageFinishedInit(ChannelHelper helper, CompoundTagAPI<?> worldData) {
        super(helper);
        this.hasWorldData = Objects.isNull(worldData) || worldData.isEmpty();
        this.worldData = worldData;
    }
    
    public MessageFinishedInit(ByteBuf buf) {
        super(buf);
        this.hasWorldData = buf.readBoolean();
        this.worldData = this.hasWorldData ? NetworkHelper.readTag(buf) : null;
    }
    
    @Override public void encode(ByteBuf buf) {
        super.encode(buf);
        buf.writeBoolean(this.hasWorldData);
        if(this.hasWorldData) NetworkHelper.writeTag(buf,this.worldData);
    }
    
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        if(Objects.nonNull(this.worldData)) this.helper.onConnected(this.worldData);
        this.helper.setSyncable(true);
        return null;
    }
}