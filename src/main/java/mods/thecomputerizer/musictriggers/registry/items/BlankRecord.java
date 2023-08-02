package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class BlankRecord extends Item {

    public BlankRecord(Item.Properties p) {
        super(p);
    }

    @Override
    @Nonnull
    public ActionResultType useOn(ItemUseContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (state.getBlock() instanceof MusicRecorder) {
            MusicRecorder recorder = (MusicRecorder) state.getBlock();
            if(!ctx.getLevel().isClientSide() && !state.getValue(MusicRecorder.HAS_RECORD) && !state.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack stack = Objects.requireNonNull(ctx.getPlayer()).getItemInHand(ctx.getHand());
                recorder.insertRecord(ctx.getLevel(),ctx.getClickedPos(),stack,state,ctx.getPlayer());
                stack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        } else return ActionResultType.PASS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable World world, List<ITextComponent> components, @Nonnull ITooltipFlag flag) {
        components.add(AssetUtil.extraLang(Constants.MODID,"item","blank_record","description",false));
    }
}
