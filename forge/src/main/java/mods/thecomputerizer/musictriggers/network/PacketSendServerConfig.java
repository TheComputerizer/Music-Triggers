package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannel;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;

public class PacketSendServerConfig extends MessageImpl {

    private final List<ServerChannel> serverChannels;
    private final List<String> disabledGuiButtons;

    public PacketSendServerConfig(FriendlyByteBuf buf) {
        this.serverChannels = NetworkUtil.readGenericList(buf,ServerChannel::new);
        this.disabledGuiButtons = NetworkUtil.readGenericList(buf,NetworkUtil::readString);
    }

    public PacketSendServerConfig(List<ServerChannel> serverChannels, List<String> disabledGuiButtons) {
        this.serverChannels = serverChannels;
        this.disabledGuiButtons = disabledGuiButtons;
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ChannelManager.addServerChannels(this.serverChannels,this.disabledGuiButtons);
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtil.writeGenericList(buf,this.serverChannels,(buf1, channel) -> channel.encode(buf1));
        NetworkUtil.writeGenericList(buf,this.disabledGuiButtons,NetworkUtil::writeString);
    }
}
