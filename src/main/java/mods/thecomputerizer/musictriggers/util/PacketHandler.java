package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.util.packets.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    public static int disc = 0;

    private  static  final  String  PROTOCOL_VERSION  =  "1.0" ;
    private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Constants.MODID, "main"))
            .clientAcceptedVersions ( PROTOCOL_VERSION :: equals)
            .serverAcceptedVersions ( PROTOCOL_VERSION :: equals)
            .networkProtocolVersion (() ->  PROTOCOL_VERSION )
            .simpleChannel ();

    public static void register() {
        HANDLER.registerMessage(disc++, PacketBossInfo.class, PacketBossInfo::encode, PacketBossInfo::new, PacketBossInfo::handle);
        HANDLER.registerMessage(disc++, PacketQueryServerInfo.class, PacketQueryServerInfo::encode, PacketQueryServerInfo::new, PacketQueryServerInfo::handle);
        HANDLER.registerMessage(disc++, PacketSyncServerInfo.class, PacketSyncServerInfo::encode, PacketSyncServerInfo::new, PacketSyncServerInfo::handle);
        HANDLER.registerMessage(disc++, PacketReceiveCommand.class, PacketReceiveCommand::encode, PacketReceiveCommand::new, PacketReceiveCommand::handle);
        HANDLER.registerMessage(disc++, PacketJukeBoxCustom.class, PacketJukeBoxCustom::encode, PacketJukeBoxCustom::new, PacketJukeBoxCustom::handle);
    }

    public static void sendTo(Object message, ServerPlayerEntity player) {
        HANDLER.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(Object message) {
        HANDLER.sendToServer(message);
    }
}
