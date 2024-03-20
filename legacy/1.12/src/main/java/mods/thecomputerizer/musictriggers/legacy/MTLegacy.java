package mods.thecomputerizer.musictriggers.legacy;

import mods.thecomputerizer.musictriggers.api.MTAPI;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerSelectorAPI;
import mods.thecomputerizer.musictriggers.legacy.client.MTClientLegacy;
import mods.thecomputerizer.musictriggers.legacy.server.MTServerLegacy;
import mods.thecomputerizer.theimpossiblelibrary.api.Reference;
import mods.thecomputerizer.theimpossiblelibrary.api.TILRef;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import java.io.IOException;

public class MTLegacy extends MTAPI {

    public static final Reference LEGACY_REF;

    static {
        try {
            LEGACY_REF = MTRef.instance(new MTLegacy(),FMLLaunchHandler.side()::isClient,
                    "required-after:"+TILRef.MODID+";");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TriggerContextAPI<?,?> getTriggerContext(ChannelAPI channel) {
        return channel.isClientChannel() ? MTClientLegacy.getTriggerContext(channel) :
                MTServerLegacy.getTriggerContext(channel);
    }

    @Override
    public <P,W> TriggerSelectorAPI<P,W> getTriggerSelector(ChannelAPI channel, TriggerContextAPI<P,W> context) {
        return channel.isClientChannel() ? MTClientLegacy.getTriggerSelector(channel,context) :
                MTServerLegacy.getTriggerSelector(channel,context);
    }
}