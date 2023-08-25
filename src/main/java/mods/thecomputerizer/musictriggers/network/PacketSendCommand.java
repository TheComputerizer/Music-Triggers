package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
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

public class PacketSendCommand extends MessageImpl {

    private final String identifier;
    private final boolean isCommandTrigger;
    private final boolean isReload;
    private final boolean isDebug;

    public PacketSendCommand(FriendlyByteBuf buf) {
        this.identifier = NetworkUtil.readString(buf);
        this.isCommandTrigger = buf.readBoolean();
        this.isReload = buf.readBoolean();
        this.isDebug = buf.readBoolean();
    }

    public PacketSendCommand(String identifier, boolean command, boolean reload, boolean debug) {
        this.identifier = identifier;
        this.isCommandTrigger = command;
        this.isReload = reload;
        this.isDebug = debug;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(Minecraft mc, ClientPacketListener listener, PacketSender sender) {
        ClientEvents.onCommand(this.identifier,this.isCommandTrigger,this.isReload,this.isDebug);
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
        return MusicTriggers.PACKET_SEND_COMMAND;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtil.writeString(buf,this.identifier);
        buf.writeBoolean(this.isCommandTrigger);
        buf.writeBoolean(this.isReload);
        buf.writeBoolean(this.isDebug);
    }
}
