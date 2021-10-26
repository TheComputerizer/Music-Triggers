package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;


public final class RegistryHandler {

    public static void registerItems(IEventBus eventBus) {
        MusicTriggers.logger.info("Loading Items from Music Triggers");
        MusicTriggersItems.INSTANCE.init();
        MusicTriggersItems.ITEMS.register(eventBus);
    }

    public static void registerSoundEvents(IEventBus eventBus)
    {
        MusicTriggers.logger.info("Loading Sounds from Music Triggers");
        ModSounds.INSTANCE.init();
        ModSounds.SOUNDS.register(eventBus);
    }


    public static void onModelRegister(ModelRegistryEvent event) {
        //for(Item i: MusicTriggersItems.INSTANCE.getItems()) {
            //ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation("musictriggers:record", "inventory"));
        //}
    }

    public static void init(IEventBus eventBus) {
        registerItems(eventBus);
        registerSoundEvents(eventBus);
    }
}
