package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
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

public class PacketFinishedServerInit extends MessageImpl {

    private final boolean isFinished;

    public PacketFinishedServerInit(FriendlyByteBuf buf) {
        this.isFinished = buf.readBoolean();
    }

    public PacketFinishedServerInit() {
        this.isFinished = true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(Minecraft mc, ClientPacketListener listener, PacketSender sender) {
        ChannelManager.finalizeServerChannelInit();
    }

    @Override
    public void handleServer(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener,
                             PacketSender sender) {}

    @Override
    public EnvType getSide() {
        return EnvType.CLIENT;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return MusicTriggers.PACKET_FINISHED_SERVER_INIT;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isFinished);
    }
}
