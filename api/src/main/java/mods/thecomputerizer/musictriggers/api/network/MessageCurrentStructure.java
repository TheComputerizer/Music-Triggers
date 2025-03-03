package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

public class MessageCurrentStructure<CTX> extends ChannelHelperMessage<CTX> {
    
    private final String structureName;
    private final String structureID;
    
    public MessageCurrentStructure(ChannelHelper helper, String structureName, String structureID) {
        super(helper);
        this.structureName = structureName;
        this.structureID = structureID;
    }
    
    public MessageCurrentStructure(ByteBuf buf) {
        super(buf);
        this.structureName = NetworkHelper.readString(buf);
        this.structureID = NetworkHelper.readString(buf);
    }
    
    @Override public void encode(ByteBuf buf) {
        super.encode(buf);
        NetworkHelper.writeString(buf,this.structureName);
        NetworkHelper.writeString(buf,this.structureID);
    }
    
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        this.helper.getDebugInfo().updateStructure(this.structureName,this.structureID);
        return null;
    }
}
