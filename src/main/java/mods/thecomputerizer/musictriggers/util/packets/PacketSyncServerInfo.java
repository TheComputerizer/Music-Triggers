package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketSyncServerInfo implements IMessageHandler<PacketSyncServerInfo.Message, IMessage> {

    @Override
    public IMessage onMessage(PacketSyncServerInfo.Message message, MessageContext ctx) {
        for(ClientSync sync : message.clientReturnInfo) ChannelManager.syncInfoFromServer(sync);
        return null;
    }

    public static class Message implements IMessage {
        private Map<String, Map<String, Boolean>> triggerStatus;
        private List<ClientSync> clientReturnInfo = new ArrayList<>();

        public Message() {
        }

        public Message(Map<String, Map<String, Boolean>> triggerStatus) {
            this.triggerStatus = triggerStatus;
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
}
