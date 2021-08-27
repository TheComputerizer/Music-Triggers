package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;

import java.util.Objects;

public class MusicTriggersRecord extends ItemRecord {

    public MusicTriggersRecord(String name, SoundEvent soundIn) {
        super(name, soundIn);
        setRegistryName(name);
        this.setTranslationKey(Objects.requireNonNull(this.getRegistryName()).toString());
        setCreativeTab(CreativeTabs.MISC);
    }
}