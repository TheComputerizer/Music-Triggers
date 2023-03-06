package mods.thecomputerizer.musictriggers.network;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.network.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    public static int disc = 0;

    private  static  final  String  PROTOCOL_VERSION  =  "1.0" ;
    private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Constants.MODID, "main"))
            .clientAcceptedVersions ( PROTOCOL_VERSION :: equals)
            .serverAcceptedVersions ( PROTOCOL_VERSION :: equals)
            .networkProtocolVersion (() ->  PROTOCOL_VERSION )
            .simpleChannel ();

    public static void register() {
        HANDLER.registerMessage(disc++, PacketDynamicChannelInfo.class, PacketDynamicChannelInfo::encode,
                PacketDynamicChannelInfo::new, PacketDynamicChannelInfo::handle);
        HANDLER.registerMessage(disc++, PacketInitChannels.class, PacketInitChannels::encode,
                PacketInitChannels::new, PacketInitChannels::handle);
        HANDLER.registerMessage(disc++, PacketJukeBoxCustom.class, PacketJukeBoxCustom::encode,
                PacketJukeBoxCustom::new, PacketJukeBoxCustom::handle);
        HANDLER.registerMessage(disc++, PacketReceiveCommand.class, PacketReceiveCommand::encode,
                PacketReceiveCommand::new, PacketReceiveCommand::handle);
        HANDLER.registerMessage(disc++, PacketSyncServerInfo.class, PacketSyncServerInfo::encode,
                PacketSyncServerInfo::new, PacketSyncServerInfo::handle);
    }

    public static void sendTo(Object message, ServerPlayer player) {
        HANDLER.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(Object message) {
        HANDLER.sendToServer(message);
    }
}
