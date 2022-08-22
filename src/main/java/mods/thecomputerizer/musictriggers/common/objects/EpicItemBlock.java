package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class EpicItemBlock extends ItemBlock {
    public EpicItemBlock(Block block) {
        super(block);
    }

    @Override
    public net.minecraftforge.common.IRarity getForgeRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }
}
