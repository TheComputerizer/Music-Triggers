package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

import java.util.Objects;

public class BlankRecord extends Item {

    public BlankRecord(Item.Properties p) {
        super(p);
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx)
    {
        BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());

        if (blockstate.getBlock() instanceof MusicRecorder)
        {
            MusicRecorder mr = (MusicRecorder) blockstate.getBlock();
            if(!ctx.getLevel().isClientSide() && !blockstate.getValue(MusicRecorder.HAS_RECORD)) {
                ItemStack itemstack = Objects.requireNonNull(ctx.getPlayer()).getItemInHand(ctx.getHand());
                mr.insertRecord(ctx.getLevel(),ctx.getClickedPos(),blockstate,itemstack,ctx.getPlayer().getUUID());
                itemstack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        }
        else
        {
            return ActionResultType.PASS;
        }
    }
}
