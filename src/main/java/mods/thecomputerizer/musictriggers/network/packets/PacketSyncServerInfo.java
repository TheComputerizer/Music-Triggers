package mods.thecomputerizer.musictriggers.network.packets;

import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PacketSyncServerInfo {

    private Map<String, Map<String, Boolean>> triggerStatus;
    private List<ClientSync> clientReturnInfo = new ArrayList<>();
    private final String curStruct;

    public PacketSyncServerInfo(PacketBuffer buf) {
        this.clientReturnInfo = NetworkUtil.readGenericList(buf,ClientSync::new);
        this.curStruct = NetworkUtil.readString(buf);
    }

    public PacketSyncServerInfo(Map<String, Map<String, Boolean>> triggerStatus, String curStruct) {
        this.triggerStatus = triggerStatus;
        this.curStruct = curStruct;
    }

    public static void encode(PacketSyncServerInfo packet, PacketBuffer buf) {
        NetworkUtil.writeGenericMap(buf,packet.triggerStatus,NetworkUtil::writeString, (buf1, map) ->
                NetworkUtil.writeGenericMap(buf1,map,NetworkUtil::writeString,PacketBuffer::writeBoolean));
        NetworkUtil.writeString(buf,packet.curStruct);
    }

    public static void handle(final PacketSyncServerInfo packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
        });
        for (ClientSync sync : packet.clientReturnInfo) ChannelManager.syncInfoFromServer(sync);
        ChannelManager.CUR_STRUCT = packet.curStruct;
        ctx.setPacketHandled(true);
    }
}
