package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class MusicTriggersRecord extends RecordItem {

    public MusicTriggersRecord(int i, SoundEvent soundIn, Item.Properties p) {
        super(i, soundIn, p.stacksTo(1).tab(CreativeModeTab.TAB_MISC));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull MutableComponent getDisplayName() {
        return new TranslatableComponent(this.getDescriptionId() + ".desc");
    }
}