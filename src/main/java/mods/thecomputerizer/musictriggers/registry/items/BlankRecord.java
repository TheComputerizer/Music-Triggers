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

import java.util.List;
import java.util.Objects;

public class BlankRecord extends Item {

    public BlankRecord(Item.Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockstate.getBlock() instanceof MusicRecorder mr) {
            if(!ctx.getLevel().isClientSide() && !blockstate.getValue(MusicRecorder.HAS_RECORD) && !blockstate.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack itemstack = Objects.requireNonNull(ctx.getPlayer()).getItemInHand(ctx.getHand());
                mr.insertRecord(ctx.getLevel(),ctx.getClickedPos(),itemstack,blockstate,ctx.getPlayer());
                itemstack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        } else return InteractionResult.PASS;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> components,
                                TooltipFlag flag) {
        components.add(MutableComponent.create(AssetUtil.extraLang(Constants.MODID,"item","blank_record",
                "description",false)));
    }
}
