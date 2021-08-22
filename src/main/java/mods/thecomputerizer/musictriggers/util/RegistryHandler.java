package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
public final class RegistryHandler {
    public static List<Item> allItems = new ArrayList<>();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt){

        IForgeRegistry<Item> r = evt.getRegistry();
        for(String s: json.allSongs) {
            Item i = new MusicTriggersRecord(s, new SoundEvent(new ResourceLocation(MusicTriggers.MODID, s)).setRegistryName(s), s);
            allItems.add(i);
            r.register(i);
        }
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        for(Item i: allItems) {
            ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation("musictriggers:record", "inventory"));
        }
    }
}
