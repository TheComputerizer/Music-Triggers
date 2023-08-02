package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketSyncServerInfo extends MessageImpl {

    private final Map<String, Map<String, Boolean>> triggerStatus;
    private final List<ClientSync> clientReturnInfo;
    private final String curStruct;

    public PacketSyncServerInfo(PacketBuffer buf) {
        this.triggerStatus = new HashMap<>();
        this.clientReturnInfo = NetworkUtil.readGenericList(buf,ClientSync::new);
        this.curStruct = NetworkUtil.readString(buf);
    }

    public PacketSyncServerInfo(Map<String, Map<String, Boolean>> triggerStatus, String curStruct) {
        this.triggerStatus = triggerStatus;
        this.clientReturnInfo = new ArrayList<>();
        this.curStruct = curStruct;
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        for(ClientSync sync : this.clientReturnInfo) ChannelManager.syncInfoFromServer(sync);
        ChannelManager.CUR_STRUCT = this.curStruct;
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(PacketBuffer buf) {
        NetworkUtil.writeGenericMap(buf,this.triggerStatus,NetworkUtil::writeString, (buf1, map) ->
                NetworkUtil.writeGenericMap(buf1,map,NetworkUtil::writeString,ByteBuf::writeBoolean));
        NetworkUtil.writeString(buf,this.curStruct);
    }
}
