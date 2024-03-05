package mods.thecomputerizer.musictriggers.network;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;

public class PacketMusicTriggersLogin extends MessageImpl {

    private boolean hasServerConfig;
    private Map<String,Map<String,Boolean>> toggleData;
    private Map<String,Map<String,Tuple<ImmutableList<String>,Integer>>> playOnceData;
    private int sortType;

    public PacketMusicTriggersLogin(FriendlyByteBuf buf) {
        ChannelManager.onClientLogin(buf);
    }

    public PacketMusicTriggersLogin(boolean hasServerConfig, Map<String,Map<String,Boolean>> toggleData,
                                    Map<String,Map<String,Tuple<ImmutableList<String>,Integer>>> playOnceData, int sortType) {
        this.hasServerConfig = hasServerConfig;
        this.toggleData = toggleData;
        this.playOnceData = playOnceData;
        this.sortType = sortType;
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {}

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.hasServerConfig);
        NetworkUtil.writeGenericMap(buf,this.toggleData,NetworkUtil::writeString,(buf1,map) ->
                NetworkUtil.writeGenericMap(buf1,map,NetworkUtil::writeString,ByteBuf::writeBoolean));
        NetworkUtil.writeGenericMap(buf,this.playOnceData,NetworkUtil::writeString,(buf1,map) ->
                NetworkUtil.writeGenericMap(buf1,map,NetworkUtil::writeString,(buf2,tuple) -> {
                    NetworkUtil.writeGenericList(buf2, tuple.getA(), NetworkUtil::writeString);
                    buf2.writeInt(tuple.getB());
                }));
        buf.writeInt(this.sortType);
    }
}
