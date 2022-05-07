package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlankRecord extends Item {

    public BlankRecord(Item.Properties p) {
        super(p);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockstate.getBlock() instanceof MusicRecorder mr) {
            if(!ctx.getLevel().isClientSide() && !blockstate.getValue(MusicRecorder.HAS_RECORD) && !blockstate.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack itemstack = Objects.requireNonNull(ctx.getPlayer()).getItemInHand(ctx.getHand());
                mr.insertRecord(ctx.getLevel(),ctx.getClickedPos(),blockstate,itemstack,ctx.getPlayer().getUUID());
                itemstack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        else return InteractionResult.PASS;
    }
}
