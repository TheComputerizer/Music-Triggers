package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.util.packets.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public class PacketHandler {

    public static void register() {
        CurSong.register();
        InfoForBiome.register();
        InfoForHome.register();
        InfoForMob.register();
        InfoForRaid.register();
        InfoForSnow.register();
        InfoForStructure.register();
        InfoFromBiome.register();
        InfoFromHome.register();
        InfoFromMob.register();
        InfoFromRaid.register();
        InfoFromSnow.register();
        InfoFromStructure.register();
        BossInfo.register();
        AllTriggers.register();
        MenuSongs.register();
        ExecuteCommand.register();
    }

    public static void sendTo(Identifier id, PacketByteBuf buf, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player,id,buf);
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(Identifier id, PacketByteBuf buf) {
        ClientPlayNetworking.send(id,buf);
    }
}
