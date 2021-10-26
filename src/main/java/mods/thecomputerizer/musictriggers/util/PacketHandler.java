package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.packets.InfoForBiome;
import mods.thecomputerizer.musictriggers.util.packets.InfoForStructure;
import mods.thecomputerizer.musictriggers.util.packets.InfoFromBiome;
import mods.thecomputerizer.musictriggers.util.packets.InfoFromStructure;
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
        HANDLER.registerMessage(disc++, InfoForStructure.class, InfoForStructure::encode, InfoForStructure::new, InfoForStructure::handle);
        HANDLER.registerMessage(disc++, InfoFromStructure.class, InfoFromStructure::encode, InfoFromStructure::new, InfoFromStructure::handle);
        HANDLER.registerMessage(disc++, InfoForBiome.class, InfoForBiome::encode, InfoForBiome::new, InfoForBiome::handle);
        HANDLER.registerMessage(disc++, InfoFromBiome.class, InfoFromBiome::encode, InfoFromBiome::new, InfoFromBiome::handle);
    }

    public static void sendTo(Object message, ServerPlayerEntity player) {
        HANDLER.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(Object message) {
        HANDLER.sendToServer(message);
    }
}
