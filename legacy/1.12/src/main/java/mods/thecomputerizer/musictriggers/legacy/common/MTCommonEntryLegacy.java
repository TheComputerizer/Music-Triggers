package mods.thecomputerizer.musictriggers.legacy.common;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.legacy.MTLegacy;
import mods.thecomputerizer.musictriggers.legacy.client.MTClientEntryLegacy;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.legacy.TILLegacy;
import mods.thecomputerizer.theimpossiblelibrary.legacy.client.TILClientEntryLegacy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Objects;

@Mod(modid = MTRef.MODID,name = MTRef.NAME,version = MTRef.VERSION)
public class MTCommonEntryLegacy extends CommonEntryPoint {

    private final CommonEntryPoint clientEntry;

    public MTCommonEntryLegacy() {
        TILLegacy.init();
        ChannelHelper.init();
        this.clientEntry = MTLegacy.LEGACY_REF.isClient() ? new MTClientEntryLegacy() : null;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if(Objects.nonNull(this.clientEntry)) ((TILClientEntryLegacy)this.clientEntry).preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if(Objects.nonNull(this.clientEntry)) ((TILClientEntryLegacy)this.clientEntry).init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPreInitializationEvent event) {
        if(Objects.nonNull(this.clientEntry)) ((TILClientEntryLegacy)this.clientEntry).postInit(event);
    }
}