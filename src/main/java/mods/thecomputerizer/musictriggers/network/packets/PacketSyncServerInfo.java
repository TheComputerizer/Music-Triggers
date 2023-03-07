package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketSyncServerInfo implements IPacket {

    private Map<String, Map<String, Boolean>> triggerStatus;
    private List<ClientSync> clientReturnInfo = new ArrayList<>();
    private final String curStruct;

    private PacketSyncServerInfo(FriendlyByteBuf buf) {
        this.clientReturnInfo = NetworkUtil.readGenericList(buf,ClientSync::new);
        this.curStruct = NetworkUtil.readString(buf);
    }

    public PacketSyncServerInfo(Map<String, Map<String, Boolean>> triggerStatus, String curStruct) {
        this.triggerStatus = triggerStatus;
        this.curStruct = curStruct;
    }

    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        NetworkUtil.writeGenericMap(buf,this.triggerStatus,NetworkUtil::writeString, (buf1, map) ->
                NetworkUtil.writeGenericMap(buf1,map,NetworkUtil::writeString,FriendlyByteBuf::writeBoolean));
        NetworkUtil.writeString(buf,this.curStruct);
        return buf;
    }

    public static ClientPlayNetworking.PlayChannelHandler handle() {
        return (client, handler, buf, sender) -> {
            PacketSyncServerInfo packet = new PacketSyncServerInfo(buf);
            for (ClientSync sync : packet.clientReturnInfo) ChannelManager.syncInfoFromServer(sync);
            ChannelManager.CUR_STRUCT = packet.curStruct;
        };
    }
}
