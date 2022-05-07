package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MusicTriggersRecord extends RecordItem {

    public MusicTriggersRecord(int i, SoundEvent soundIn, Item.Properties p) {
        super(i, soundIn, p.stacksTo(1).tab(CreativeModeTab.TAB_MISC));
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
        else return super.useOn(ctx);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull MutableComponent getDisplayName() {
        return new TranslatableComponent(this.getDescriptionId() + ".desc");
    }
}