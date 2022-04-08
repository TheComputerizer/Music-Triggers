package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class MusicTriggersRecord extends MusicDiscItem {

    public MusicTriggersRecord(int i, SoundEvent soundIn, Item.Settings s) {
        super(i, soundIn, s.maxCount(1).group(ItemGroup.MISC));
    }

    @Override
    public @NotNull ActionResult useOnBlock(ItemUsageContext ctx) {
        BlockState blockstate = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (blockstate.getBlock() instanceof MusicRecorder mr) {
            if(!ctx.getWorld().isClient() && !blockstate.get(MusicRecorder.HAS_RECORD) && !blockstate.get(MusicRecorder.HAS_DISC)) {
                ItemStack itemstack = ctx.getStack();
                mr.insertRecord(ctx.getWorld(),ctx.getBlockPos(),blockstate,itemstack, Objects.requireNonNull(ctx.getPlayer()).getUuid());
                itemstack.decrement(1);
            }
            return ActionResult.SUCCESS;
        }
        else return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(this.getDescription().formatted(Formatting.GRAY));
    }

    public MutableText getDescription() {
        return new TranslatableText(this.getTranslationKey() + ".desc");
    }
}