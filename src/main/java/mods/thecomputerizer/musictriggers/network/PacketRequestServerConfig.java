package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.server.channels.ServerChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
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

import java.util.Objects;

public class PacketRequestServerConfig extends MessageImpl {

    private final boolean sendConfig;

    public PacketRequestServerConfig(FriendlyByteBuf buf) {
        this.sendConfig = buf.readBoolean();
    }

    public PacketRequestServerConfig() {
        this.sendConfig = true;
    }

    private PacketSendServerConfig makeSendPacket() {
        return new PacketSendServerConfig(ServerChannelManager.getChannels(),ServerChannelManager.getDisabledGuiButtons());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(Minecraft mc, ClientPacketListener listener, PacketSender sender) {}

    @Override
    public void handleServer(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener,
                             PacketSender sender) {
        if(this.sendConfig && Objects.nonNull(player)) makeSendPacket().addPlayers(player).send();
    }

    @Override
    public EnvType getSide() {
        return EnvType.SERVER;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return MusicTriggers.PACKET_REQUEST_SERVER_CONFIG;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.sendConfig);
    }
}
