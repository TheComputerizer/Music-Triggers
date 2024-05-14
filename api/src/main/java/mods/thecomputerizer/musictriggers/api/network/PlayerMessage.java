package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

@Setter @Getter
public abstract class PlayerMessage<CTX> extends MessageAPI<CTX> {
    
    protected boolean login;
    protected String uuid;
    
    protected PlayerMessage(String uuid) {
        this.uuid = uuid;
    }
    
    protected PlayerMessage(ByteBuf buf) {
        this.uuid = NetworkHelper.readString(buf);
        this.login = buf.readBoolean();
    }
    
    @Override public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,this.uuid);
        buf.writeBoolean(this.login);
    }
}
