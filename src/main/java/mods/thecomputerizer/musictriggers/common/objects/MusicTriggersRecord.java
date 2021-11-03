package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.SoundEvent;

public class MusicTriggersRecord extends MusicDiscItem {

    public MusicTriggersRecord(int i, SoundEvent soundIn, Item.Properties p) {
        super(i, soundIn, p.stacksTo(1).tab(ItemGroup.TAB_MISC));
    }
}