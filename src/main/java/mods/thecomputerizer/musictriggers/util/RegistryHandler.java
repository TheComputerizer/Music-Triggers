package mods.thecomputerizer.musictriggers.util;

import com.sun.xml.internal.bind.annotation.OverrideAnnotationOf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
public final class RegistryHandler {

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e)
    {
        MusicTriggersItems.INSTANCE.init();
        e.getRegistry().registerAll(MusicTriggersItems.INSTANCE.getItems().toArray(new Item[0]));
    }

    @SubscribeEvent
    public static void onRegisterSoundEvents(RegistryEvent.Register<SoundEvent> e)
    {
        ModSounds.INSTANCE.init();
        e.getRegistry().registerAll(ModSounds.INSTANCE.getSounds().toArray(new SoundEvent[0]));
    }
}
