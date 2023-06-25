package mods.thecomputerizer.musictriggers.server;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.server.data.IPersistentTriggerData;
import mods.thecomputerizer.musictriggers.server.data.PersistentTriggerDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public class PersistentDataHandler {

    public static final ResourceLocation PERSISTANCE_TRIGGER_DATA = MusicTriggers.res("persistent_trigger_data");

    @SuppressWarnings("ConstantConditions")
    public static IPersistentTriggerData getDataCapability(EntityPlayerMP player) {
        return player.getCapability(PersistentTriggerDataProvider.PERSISTANCE_TRIGGER_DATA,null);
    }
}
