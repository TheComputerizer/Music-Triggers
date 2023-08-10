package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.server.channels.ServerChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;

public class PacketRequestServerConfig extends MessageImpl {

    private final boolean sendConfig;

    public PacketRequestServerConfig(FriendlyByteBuf buf) {
        this.sendConfig = buf.readBoolean();
    }

    public PacketRequestServerConfig() {
        this.sendConfig = true;
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        if(this.sendConfig && Objects.nonNull(ctx.getSender())) makeSendPacket().addPlayers(ctx.getSender()).send();
    }

    private PacketSendServerConfig makeSendPacket() {
        return new PacketSendServerConfig(ServerChannelManager.getChannels(),ServerChannelManager.getDisabledGuiButtons());
    }

    @Override
    public Dist getSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.sendConfig);
    }
}
