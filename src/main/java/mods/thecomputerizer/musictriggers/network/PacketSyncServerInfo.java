package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketSyncServerInfo extends MessageImpl {

    private final Map<String, Map<String, Boolean>> triggerStatus;
    private List<ClientSync> clientReturnInfo;

    public PacketSyncServerInfo() {
        this.triggerStatus = new HashMap<>();
        this.clientReturnInfo = new ArrayList<>();
    }

    public PacketSyncServerInfo(Map<String, Map<String, Boolean>> triggerStatus) {
        this.triggerStatus = triggerStatus;
        this.clientReturnInfo = new ArrayList<>();
    }

    @Override
    public IMessage handle(MessageContext ctx) {
        for(ClientSync sync : this.clientReturnInfo) ChannelManager.syncInfoFromServer(sync);
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.clientReturnInfo = NetworkUtil.readGenericList(buf,ClientSync::new);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkUtil.writeGenericMap(buf,this.triggerStatus,NetworkUtil::writeString, (buf1, map) ->
                NetworkUtil.writeGenericMap(buf1,map,NetworkUtil::writeString,ByteBuf::writeBoolean));
    }
}
