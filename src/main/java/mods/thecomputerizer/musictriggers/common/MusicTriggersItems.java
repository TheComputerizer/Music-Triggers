package mods.thecomputerizer.musictriggers.common;

import com.google.common.collect.Lists;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;

import java.util.List;
import java.util.Objects;

public class MusicTriggersItems {
    public static List<Item> allItems;
    public static final MusicTriggersItems INSTANCE = new MusicTriggersItems();

    public void init() {
        SoundHandler.registerSounds();
        allItems = Lists.newArrayList();
        for (SoundEvent s : SoundHandler.allSoundEvents) {
            Item i = (new MusicTriggersRecord(Objects.requireNonNull(s.getRegistryName()).toString().replace("musictriggers:", ""), s));
            if(!allItems.contains(i)) {
                allItems.add(i);
            }
        }
    }

    public List<Item> getItems(){
        return allItems;
    }
}
