package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.packets.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;


public class PacketHandler {
    public static int disc = 0;

    private  static  final  String  PROTOCOL_VERSION  =  "1.0" ;
    private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MusicTriggers.MODID, "main"))
            .clientAcceptedVersions ( PROTOCOL_VERSION :: equals)
            .serverAcceptedVersions ( PROTOCOL_VERSION :: equals)
            .networkProtocolVersion (() ->  PROTOCOL_VERSION )
            .simpleChannel ();

    public static void register() {
        HANDLER.registerMessage(disc++, PacketBossInfo.class, PacketBossInfo::encode, PacketBossInfo::new, PacketBossInfo::handle);
        HANDLER.registerMessage(disc++, PacketQueryServerInfo.class, PacketQueryServerInfo::encode, PacketQueryServerInfo::new, PacketQueryServerInfo::handle);
        HANDLER.registerMessage(disc++, PacketSyncServerInfo.class, PacketSyncServerInfo::encode, PacketSyncServerInfo::new, PacketSyncServerInfo::handle);
        HANDLER.registerMessage(disc++, PacketReceiveCommand.class, PacketReceiveCommand::encode, PacketReceiveCommand::new, PacketReceiveCommand::handle);
    }

    public static void sendTo(Object message, ServerPlayer player) {
        HANDLER.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(Object message) {
        HANDLER.sendToServer(message);
    }
}
