package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos,
                                      @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        if(player instanceof EntityPlayerMP) {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            if (iblockstate.getBlock() instanceof MusicRecorder) {
                MusicRecorder mr = (MusicRecorder) iblockstate.getBlock();
                if (!worldIn.isRemote && !iblockstate.getValue(MusicRecorder.HAS_RECORD) && !iblockstate.getValue(MusicRecorder.HAS_DISC)) {
                    ItemStack itemstack = player.getHeldItem(hand);
                    mr.insertRecord(worldIn, pos, iblockstate, itemstack, (EntityPlayerMP)player);
                    itemstack.shrink(1);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip,
                               @Nonnull ITooltipFlag flagIn) {
        tooltip.add(AssetUtil.extraLang(Constants.MODID,"item","blank_record","description"));
    }
}
