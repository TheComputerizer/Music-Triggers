package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.item.*;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class MusicTriggersItems {
    public static final MusicTriggersItems INSTANCE = new MusicTriggersItems();
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MusicTriggers.MODID);

    public void init() {
        SoundHandler.registerSounds();
        for (SoundEvent s : SoundHandler.allSoundEvents) {
            ITEMS.register(s.getRegistryName().toString().replaceAll("musictriggers:",""), () -> new MusicDiscItem(15,s,new Item.Properties().rarity(Rarity.EPIC).stacksTo(1).fireResistant()));
        }
    }
}
