package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlankRecord extends Item {

    public BlankRecord(Item.Settings s) {
        super(s);
    }

    @Override
    public @NotNull ActionResult useOnBlock(ItemUsageContext ctx)
    {
        BlockState blockstate = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (blockstate.getBlock() instanceof MusicRecorder mr)
        {
            if(!ctx.getWorld().isClient() && !blockstate.get(MusicRecorder.HAS_RECORD)) {
                ItemStack itemstack = ctx.getStack();
                mr.insertRecord(ctx.getWorld(),ctx.getBlockPos(),blockstate,itemstack, Objects.requireNonNull(ctx.getPlayer()).getUuid());
                itemstack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }
        else
        {
            return ActionResult.PASS;
        }
    }
}
