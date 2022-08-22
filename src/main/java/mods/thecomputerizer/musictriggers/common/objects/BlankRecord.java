package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("deprecation")
public class BlankRecord extends EpicItem {

    public BlankRecord() {}

    @SuppressWarnings("NullableProblems")
    @Override
    public EnumActionResult onItemUse( EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        if (iblockstate.getBlock() instanceof MusicRecorder) {
            MusicRecorder mr = (MusicRecorder) iblockstate.getBlock();
            if(!worldIn.isRemote && !iblockstate.getValue(MusicRecorder.HAS_RECORD) && !iblockstate.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack itemstack = player.getHeldItem(hand);
                mr.insertRecord(worldIn,pos,iblockstate,itemstack,player.getUniqueID());
                itemstack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }
        else return EnumActionResult.PASS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.translateToLocal(getUnlocalizedName(stack) + ".description"));
    }
}
