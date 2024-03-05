package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class PacketDynamicChannelInfo extends MessageImpl {

    private final List<Channel> clientChannels = new ArrayList<>();

    public PacketDynamicChannelInfo(FriendlyByteBuf buf) {
        ServerTriggerStatus.decodeDynamicInfo(buf);
    }

    public PacketDynamicChannelInfo(List<Channel> clientChannels) {
        this.clientChannels.addAll(clientChannels);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {}

    @Override
    public Dist getSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtil.writeString(buf, Minecraft.getInstance().player.getStringUUID());
        NetworkUtil.writeGenericList(buf,this.clientChannels,(buf1, channel) -> channel.encodeDynamic(buf1));
        buf.writeInt(Instance.getPreferredSort());
    }
}
