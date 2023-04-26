package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.server.data.ServerChannels;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ServerEvents {
    private static int TIMER = 0;

    public static void onServerTick() {
        TIMER++;
        if (TIMER >= 5) {
            ServerChannels.runServerChecks();
            TIMER = 0;
        }
    }

    public static InteractionResult livingAttack(Player player, Entity target) {
        if(player instanceof ServerPlayer && target instanceof ServerPlayer)
            ServerChannels.setPVP((ServerPlayer)player,target.getUUID().toString());
        return InteractionResult.PASS;
    }
}
