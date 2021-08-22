package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;

public class MusicTriggersRecord extends ItemRecord {

    public MusicTriggersRecord(String unlocalizedName, SoundEvent soundIn, String registryName) {
        super(unlocalizedName, soundIn);
        setTranslationKey(MusicTriggers.MODID + "." + unlocalizedName);
        setRegistryName(registryName);
        setCreativeTab(CreativeTabs.MISC);
    }
}