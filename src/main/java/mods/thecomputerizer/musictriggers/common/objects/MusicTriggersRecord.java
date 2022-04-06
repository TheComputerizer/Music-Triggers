package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class MusicTriggersRecord extends ItemRecord {

    public MusicTriggersRecord(String name, SoundEvent soundIn) {
        super(name, soundIn);
        setRegistryName(name);
        this.setUnlocalizedName(Objects.requireNonNull(this.getRegistryName()).toString());
        setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() instanceof MusicRecorder)
        {
            MusicRecorder mr = (MusicRecorder) iblockstate.getBlock();
            if(!worldIn.isRemote && !iblockstate.getValue(MusicRecorder.HAS_RECORD) && !iblockstate.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack itemstack = player.getHeldItem(hand);
                mr.insertRecord(worldIn,pos,iblockstate,itemstack,player.getUniqueID());
                itemstack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }
        else
        {
            return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
    }
}