package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import mods.thecomputerizer.musictriggers.server.data.PersistentTriggerDataProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.IOException;

public class ServerEvents {

    private static final MutableInt TIMER = new MutableInt();

    public static void onServerTick() {
        if (TIMER.incrementAndGet() >= 5) {
            ServerTriggerStatus.runServerChecks();
            TIMER.setValue(0);
        }
    }

    public static void onServerStarting(MinecraftServer server) {
        try {
            PersistentTriggerDataProvider.getInstance().readFromNBT(server.storageSource.getLevelId());
        } catch (IOException ex) {
            Constants.MAIN_LOG.error("Failed to read server trigger data!",ex);
        }
    }

    public static void onServerStopping(MinecraftServer server) {
        PersistentTriggerDataProvider.getInstance().writeToNBT(server.storageSource.getLevelId());
    }

    public static boolean onEntityAdded(Entity entity) {
        if(entity instanceof LivingEntity)
            ServerTriggerStatus.checkIfBossSpawned((LivingEntity)entity);
        return true;
    }

    public static InteractionResult onPlayerAttackEntity(Player player, Entity entity) {
        if(player instanceof ServerPlayer playerSource && entity instanceof ServerPlayer)
            ServerTriggerStatus.setPVP(playerSource,entity.getStringUUID());
        return InteractionResult.PASS;
    }

    public static void onPlayerLogin(ServerPlayer player) {
        PersistentTriggerDataProvider.getPlayerData(player).onLogin(player);
    }
}
