package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlankRecord extends Item {

    public BlankRecord() {}

    @SuppressWarnings("NullableProblems")
    @Override
    public EnumActionResult onItemUse( EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() instanceof MusicRecorder)
        {
            MusicRecorder mr = (MusicRecorder) iblockstate.getBlock();
            if(!worldIn.isRemote && !iblockstate.getValue(BlockJukebox.HAS_RECORD)) {
                ItemStack itemstack = player.getHeldItem(hand);
                mr.insertRecord(worldIn,pos,iblockstate,itemstack,player.getUniqueID());
                itemstack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.PASS;
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public net.minecraftforge.common.IRarity getForgeRarity(ItemStack stack)
    {
        return EnumRarity.EPIC;
    }
}
