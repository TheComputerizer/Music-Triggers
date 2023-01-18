package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IRarity;

import javax.annotation.Nonnull;

public class EpicItemBlock extends ItemBlock {
    public EpicItemBlock(Block block) {
        super(block);
    }

    @Override
    @Nonnull
    public IRarity getForgeRarity(@Nonnull ItemStack stack) {
        return EnumRarity.EPIC;
    }
}
