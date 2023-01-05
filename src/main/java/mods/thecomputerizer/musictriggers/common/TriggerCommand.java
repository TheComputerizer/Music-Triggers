package mods.thecomputerizer.musictriggers.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketReceiveCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public class TriggerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("triggercommand")
                .then(Commands.argument("identifier", StringArgumentType.word())
                .executes((ctx) -> {
                    try {
                        PacketHandler.sendTo(new PacketReceiveCommand(StringArgumentType.getString(ctx, "identifier")), ctx.getSource().getPlayerOrException());
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
                                        PacketHandler.sendTo(new PacketReceiveCommand(StringArgumentType.getString(ctx, "identifier")), EntityArgument.getPlayer(ctx,"player"));
                                        return 1;
                                    } catch (Exception e) {
                                        Constants.MAIN_LOG.error("Player not specified correctly");
                                        e.printStackTrace();
                                    }
                                    return 0;
                                }))));
    }
}
