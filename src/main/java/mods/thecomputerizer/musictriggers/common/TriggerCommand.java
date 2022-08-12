package mods.thecomputerizer.musictriggers.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketReceiveCommand;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TriggerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("triggercommand")
                .then(CommandManager.argument("identifier", StringArgumentType.word())
                .executes((ctx) -> {
                    try {
                        PacketHandler.sendTo(PacketReceiveCommand.id,PacketReceiveCommand.encode(new PacketReceiveCommand(StringArgumentType.getString(ctx, "identifier"))), ctx.getSource().getPlayer());
                        return 1;
                    } catch (Exception e) {
                        MusicTriggers.logger.error("Player not specified");
                        e.printStackTrace();
                    }
                    return 0;
                }))
                .then(CommandManager.argument("identifier", StringArgumentType.word())
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes((ctx) -> {
                                    try {
                                        PacketHandler.sendTo(PacketReceiveCommand.id,PacketReceiveCommand.encode(new PacketReceiveCommand(StringArgumentType.getString(ctx, "identifier"))), EntityArgumentType.getPlayer(ctx,"player"));
                                        return 1;
                                    } catch (Exception e) {
                                        MusicTriggers.logger.error("Player not specified correctly");
                                        e.printStackTrace();
                                    }
                                    return 0;
                                }))));
    }
}
