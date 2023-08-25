package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
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

import java.util.ArrayList;
import java.util.List;

public class PacketDynamicChannelInfo extends MessageImpl {

    private final List<Channel> clientChannels = new ArrayList<>();
    private FriendlyByteBuf storedBuf;

    public PacketDynamicChannelInfo(FriendlyByteBuf buf) {
        this.storedBuf = buf;
    }

    public PacketDynamicChannelInfo(List<Channel> clientChannels) {
        this.clientChannels.addAll(clientChannels);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(Minecraft mc, ClientPacketListener listener, PacketSender sender) {}

    @Override
    public void handleServer(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener,
                             PacketSender sender) {
        ServerTriggerStatus.decodeDynamicInfo(server,this.storedBuf);
    }

    @Override
    public EnvType getSide() {
        return EnvType.SERVER;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return MusicTriggers.PACKET_DYNAMIC_CHANNEL_INFO;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtil.writeString(buf, Minecraft.getInstance().player.getStringUUID());
        NetworkUtil.writeGenericList(buf,this.clientChannels,(buf1, channel) -> channel.encodeDynamic(buf1));
        buf.writeInt(Instance.getPreferredSort());
    }
}
