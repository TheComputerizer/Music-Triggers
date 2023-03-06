package mods.thecomputerizer.musictriggers.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.network.packets.PacketReceiveCommand;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;

public class TriggerCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("triggercommand")
                .then(Commands.argument("identifier", StringArgumentType.word())
                .executes((ctx) -> {
                    try {
                        NetworkHandler.sendTo(new PacketReceiveCommand(StringArgumentType.getString(ctx, "identifier")), ctx.getSource().getPlayerOrException());
                        return 1;
                    } catch (Exception e) {
                        Constants.MAIN_LOG.error("Player not specified");
                        e.printStackTrace();
                    }
                    return 0;
                }))
                .then(Commands.argument("identifier", StringArgumentType.word())
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes((ctx) -> {
                                    try {
                                        NetworkHandler.sendTo(new PacketReceiveCommand(StringArgumentType.getString(ctx, "identifier")), EntityArgument.getPlayer(ctx,"player"));
                                        return 1;
                                    } catch (Exception e) {
                                        Constants.MAIN_LOG.error("Player not specified correctly");
                                        e.printStackTrace();
                                    }
                                    return 0;
                                }))));
    }
}
