package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class BlankRecord extends EpicItem {

    public BlankRecord() {}

    @Override
    @Nonnull
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos,
                                      @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        if(!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof MusicRecorder) {
                MusicRecorder recorder = (MusicRecorder) state.getBlock();
                if (!state.getValue(MusicRecorder.HAS_RECORD) && !state.getValue(MusicRecorder.HAS_DISC)) {
                    ItemStack stack = player.getHeldItem(hand);
                    recorder.insertRecord(world,pos,stack,player);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, List<String> tooltip,
                               @Nonnull ITooltipFlag flag) {
        tooltip.add(AssetUtil.extraLang(Constants.MODID,"item","blank_record","description",false));
    }
}
