package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
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

public class PacketInitChannels extends MessageImpl {

    private ServerTriggerStatus data;
    private FriendlyByteBuf storedBuf;

    public PacketInitChannels(FriendlyByteBuf buf) {
        this.storedBuf = buf;
    }

    public PacketInitChannels(ServerTriggerStatus channelData) {
        this.data = channelData;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(Minecraft mc, ClientPacketListener listener, PacketSender sender) {}

    @Override
    public void handleServer(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener,
                             PacketSender sender) {
        ServerTriggerStatus.initializePlayerChannels(server,this.storedBuf);
    }

    @Override
    public EnvType getSide() {
        return EnvType.SERVER;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return MusicTriggers.PACKET_INIT_CHANNELS;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        this.data.encodeForServer(buf);
    }
}
