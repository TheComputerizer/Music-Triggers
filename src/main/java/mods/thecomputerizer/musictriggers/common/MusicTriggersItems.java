package mods.thecomputerizer.musictriggers.common;

import com.google.common.collect.Lists;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
public final class MusicTriggersItems {
    public static List<Item> allItems;
    public static final MusicTriggersItems INSTANCE = new MusicTriggersItems();

    public static SoundEvent sound;

    public void init() {
        SoundHandler.registerSounds();
        allItems = Lists.newArrayList();
        for (SoundEvent s : SoundHandler.allSoundEvents) {
            System.out.print("FUCK "+s.getRegistryName().toString());
            sound = new SoundEvent(Objects.requireNonNull(s.getRegistryName()));
            allItems.add(new MusicTriggersRecord(Objects.requireNonNull(s.getRegistryName()).toString().replace("musictriggers:", ""), sound));
        }
    }

    public List<Item> getItems(){
        return allItems;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        for(Item i: allItems) {
            ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation("musictriggers:record", "inventory"));
        }
    }
}
