package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannel;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

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

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(Minecraft mc, ClientPacketListener listener, PacketSender sender) {}

    @Override
    public void handleServer(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener,
                             PacketSender sender) {
        ChannelManager.addServerChannels(this.serverChannels,this.disabledGuiButtons);
    }

    @Override
    public EnvType getSide() {
        return EnvType.CLIENT;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return MusicTriggers.PACKET_SEND_SERVER_CONFIG;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtil.writeGenericList(buf,this.serverChannels,(buf1, channel) -> channel.encode(buf1));
        NetworkUtil.writeGenericList(buf,this.disabledGuiButtons,NetworkUtil::writeString);
    }
}
