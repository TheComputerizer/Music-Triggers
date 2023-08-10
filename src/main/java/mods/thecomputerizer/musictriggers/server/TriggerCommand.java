package mods.thecomputerizer.musictriggers.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import mods.thecomputerizer.musictriggers.network.PacketSendCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class TriggerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerAliases(dispatcher,"musictriggers","mt");
    }

    private static void registerAliases(CommandDispatcher<CommandSourceStack> dispatcher, String ... aliases) {
        for(String alias : aliases)
            dispatcher.register(Commands.literal(alias)
                    .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("type", StringArgumentType.word())
                                    .executes(TriggerCommand::execute))
                            .then(Commands.argument("type", StringArgumentType.word())
                                    .then(Commands.argument("identifier", StringArgumentType.word())
                                            .executes(TriggerCommand::execute)))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player;
        try {
            player = EntityArgument.getPlayer(ctx,"player");
        } catch (Exception e) {
            player = null;
        }
        String type;
        try {
            type = StringArgumentType.getString(ctx, "type");
        } catch (Exception e) {
            type = null;
        }
        if(Objects.isNull(player) || Objects.isNull(type)) throwException("command.musictriggers.help");
        if(type.matches("reload")) {
            send(player,"not_set",false,true,false);
            return 1;
        }
        if(type.matches("debug")) {
            send(player,"not_set",false,false,true);
            return 1;
        }

        if(type.matches("commandtrigger")) {
            String identifier;
            try {
                identifier = StringArgumentType.getString(ctx, "identifier");
            } catch (Exception e) {
                identifier = null;
            }
            if(Objects.isNull(identifier)) throwException("command.musictriggers.trigger.error");
            else if(identifier.matches("not_set")) throwException("command.musictriggers.trigger.not_set");
            else send(player,identifier,true,false,false);
            return 1;
        }
        throwException("command.musictriggers.help");
        return 0;
    }

    private static void throwException(String langKey) throws CommandSyntaxException {
        throw new SimpleCommandExceptionType(new TranslatableComponent(langKey)).create();
    }

    private static void send(ServerPlayer player, String identifier, boolean isCommandTrigger, boolean isReload, boolean isDebug) {
        new PacketSendCommand(identifier,isCommandTrigger,isReload,isDebug).addPlayers(player).send();
    }
}
