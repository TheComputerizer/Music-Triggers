package mods.thecomputerizer.musictriggers.network;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

public class PacketMusicTriggersLogin extends MessageImpl {

    private boolean hasServerConfig;
    private Map<String,Map<String,Boolean>> toggleData;
    private Map<String,Map<String,ImmutableList<String>>> playOnceData;

    public PacketMusicTriggersLogin() {}

    public PacketMusicTriggersLogin(boolean hasServerConfig, Map<String,Map<String,Boolean>> toggleData,
                                    Map<String,Map<String,ImmutableList<String>>> playOnceData) {
        this.hasServerConfig = hasServerConfig;
        this.toggleData = toggleData;
        this.playOnceData = playOnceData;
    }

    @Override
    public IMessage handle(MessageContext messageContext) {
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ChannelManager.onClientLogin(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.hasServerConfig);
        NetworkUtil.writeGenericMap(buf,this.toggleData,NetworkUtil::writeString,(buf1,map) ->
                NetworkUtil.writeGenericMap(buf1,map,NetworkUtil::writeString,ByteBuf::writeBoolean));
        NetworkUtil.writeGenericMap(buf,this.playOnceData,NetworkUtil::writeString,(buf1,map) ->
                NetworkUtil.writeGenericMap(buf1,map,NetworkUtil::writeString,(buf2,list) ->
                        NetworkUtil.writeGenericList(buf2,list,NetworkUtil::writeString)));
    }
}
