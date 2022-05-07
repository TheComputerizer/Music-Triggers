package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

public class MusicTriggersRecord extends MusicDiscItem {

    public MusicTriggersRecord(int i, SoundEvent soundIn, Item.Properties p) {
        super(i, soundIn, p.stacksTo(1).tab(ItemGroup.TAB_MISC));
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx)
    {
        BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockstate.getBlock() instanceof MusicRecorder) {
            MusicRecorder mr = (MusicRecorder) blockstate.getBlock();
            if(!ctx.getLevel().isClientSide() && !blockstate.getValue(MusicRecorder.HAS_RECORD) && !blockstate.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack itemstack = Objects.requireNonNull(ctx.getPlayer()).getItemInHand(ctx.getHand());
                mr.insertRecord(ctx.getLevel(),ctx.getClickedPos(),blockstate,itemstack,ctx.getPlayer().getUUID());
                itemstack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        } else return super.useOn(ctx);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public IFormattableTextComponent getDisplayName() {
        return new TranslationTextComponent(this.getDescriptionId() + ".desc");
    }
}