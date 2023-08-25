package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Objects;

public class PacketJukeBoxCustom extends MessageImpl {

    private final BlockPos pos;
    private final boolean start;
    private final String channel;
    private final String id;

    public PacketJukeBoxCustom(FriendlyByteBuf buf) {
        this.start = buf.readBoolean();
        this.channel = NetworkUtil.readString(buf);
        this.id = NetworkUtil.readString(buf);
        this.pos = this.start ? BlockPos.of(buf.readLong()) : null;
    }

    public PacketJukeBoxCustom(BlockPos pos, String channel, String id) {
        this.start = Objects.nonNull(pos);
        this.pos = pos;
        this.channel = channel;
        this.id = id;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(Minecraft mc, ClientPacketListener listener, PacketSender sender) {
        ChannelManager.playCustomJukeboxSong(this.start, this.channel, this.id, this.pos);
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
        return MusicTriggers.PACKET_JUKEBOX_CUSTOM;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.start);
        NetworkUtil.writeString(buf,this.channel);
        NetworkUtil.writeString(buf,this.id);
        if(Objects.nonNull(this.pos)) buf.writeLong(this.pos.asLong());
    }
}
