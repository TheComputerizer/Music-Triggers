package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
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
            .named(new ResourceLocation(MusicTriggers.MODID, "main"))
            .clientAcceptedVersions ( PROTOCOL_VERSION :: equals)
            .serverAcceptedVersions ( PROTOCOL_VERSION :: equals)
            .networkProtocolVersion (() ->  PROTOCOL_VERSION )
            .simpleChannel ();

    public static void register() {
        HANDLER.registerMessage(disc++, SendTriggerData.class, SendTriggerData::encode, SendTriggerData::new, SendTriggerData::handle);
        HANDLER.registerMessage(disc++, BossInfo.class, BossInfo::encode, BossInfo::new, BossInfo::handle);
        HANDLER.registerMessage(disc++, CurSong.class, CurSong::encode, CurSong::new, CurSong::handle);
        HANDLER.registerMessage(disc++, SendTriggerData.class, SendTriggerData::encode, SendTriggerData::new, SendTriggerData::handle);
        HANDLER.registerMessage(disc++, ReturnTriggerData.class, ReturnTriggerData::encode, ReturnTriggerData::new, ReturnTriggerData::handle);
        HANDLER.registerMessage(disc++, ExecuteCommand.class, ExecuteCommand::encode, ExecuteCommand::new, ExecuteCommand::handle);
        HANDLER.registerMessage(disc++, MenuSongs.class, MenuSongs::encode, MenuSongs::new, MenuSongs::handle);
    }

    public static void sendTo(Object message, ServerPlayerEntity player) {
        HANDLER.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(Object message) {
        HANDLER.sendToServer(message);
    }
}
