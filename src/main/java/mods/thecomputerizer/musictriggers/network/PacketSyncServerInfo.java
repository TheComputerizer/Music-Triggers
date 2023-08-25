package mods.thecomputerizer.musictriggers.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientSync;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketSyncServerInfo extends MessageImpl {

    private final Map<String, Map<String, Boolean>> triggerStatus;
    private final List<ClientSync> clientReturnInfo;
    private final String curStruct;

    public PacketSyncServerInfo(FriendlyByteBuf buf) {
        this.triggerStatus = new HashMap<>();
        this.clientReturnInfo = NetworkUtil.readGenericList(buf,ClientSync::new);
        this.curStruct = NetworkUtil.readString(buf);
    }

    public PacketSyncServerInfo(Map<String, Map<String, Boolean>> triggerStatus, String curStruct) {
        this.triggerStatus = triggerStatus;
        this.clientReturnInfo = new ArrayList<>();
        this.curStruct = curStruct;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(Minecraft mc, ClientPacketListener listener, PacketSender sender) {
        for(ClientSync sync : this.clientReturnInfo) ChannelManager.syncInfoFromServer(sync);
        ChannelManager.CUR_STRUCT = this.curStruct;
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
        return MusicTriggers.PACKET_SYNC_SERVER_INFO;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        NetworkUtil.writeGenericMap(buf,this.triggerStatus,NetworkUtil::writeString, (buf1, map) ->
                NetworkUtil.writeGenericMap(buf1,map,NetworkUtil::writeString,ByteBuf::writeBoolean));
        NetworkUtil.writeString(buf,this.curStruct);
    }
}
