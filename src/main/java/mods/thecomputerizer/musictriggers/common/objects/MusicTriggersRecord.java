package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MusicTriggersRecord extends MusicDiscItem {

    public MusicTriggersRecord(int i, SoundEvent soundIn, Item.Properties p) {
        super(i, soundIn, p.stacksTo(1).tab(ItemGroup.TAB_MISC));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public IFormattableTextComponent getDisplayName() {
        return new TranslationTextComponent(this.getDescriptionId() + ".desc");
    }
}