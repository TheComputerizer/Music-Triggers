package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class EpicItem extends Item {
    @Override
    public net.minecraftforge.common.IRarity getForgeRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }
}
