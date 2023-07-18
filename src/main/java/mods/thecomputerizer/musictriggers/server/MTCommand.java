package mods.thecomputerizer.musictriggers.server;

import net.minecraft.command.ICommandSender;

import javax.annotation.Nonnull;

public class MTCommand extends MusicTriggersCommand {

    @Override
    @Nonnull
    public String getName() {
        return "mt";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "MT commands initiated";
    }
}
