package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@GameRegistry.ObjectHolder(MusicTriggers.MODID)
@Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
public final class RegistryHandler {

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e)
    {
        System.out.print("When do items happen?\n");
        MusicTriggersItems.INSTANCE.init();
        e.getRegistry().registerAll(MusicTriggersItems.INSTANCE.getItems().toArray(new Item[0]));
        for(Item i: MusicTriggersItems.INSTANCE.getItems()) {
            System.out.print(i.getRegistryName()+"\n");
        }
    }

    @SubscribeEvent
    public static void onRegisterSoundEvents(RegistryEvent.Register<SoundEvent> e)
    {
        System.out.print("When does this happen?\n");
        ModSounds.INSTANCE.init();
        e.getRegistry().registerAll(ModSounds.INSTANCE.getSounds().toArray(new SoundEvent[0]));
        for(SoundEvent s: ModSounds.INSTANCE.getSounds()) {
            System.out.print(s.getRegistryName()+"\n");
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        for(Item i: MusicTriggersItems.INSTANCE.getItems()) {
            ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation("musictriggers:record", "inventory"));
        }
    }
}
