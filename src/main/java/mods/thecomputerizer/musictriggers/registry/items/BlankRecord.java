package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlankRecord extends Item {

    public BlankRecord(Item.Properties p) {
        super(p);
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (state.getBlock() instanceof MusicRecorder recorder) {
            if(!ctx.getLevel().isClientSide() && !state.getValue(MusicRecorder.HAS_RECORD) && !state.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
                recorder.insertRecord(ctx.getLevel(),ctx.getClickedPos(),stack,state,ctx.getPlayer());
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        } else return InteractionResult.PASS;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, List<Component> components, @NotNull TooltipFlag flag) {
        components.add(MutableComponent.create(AssetUtil.extraLang(
                Constants.MODID,"item","blank_record","description",false)));
    }
}
