package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.common.EventsCommon;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.UUID;

public class MusicRecorder extends Block {

    public static final PropertyBool HAS_RECORD = PropertyBool.create("has_record");
    public static final PropertyBool HAS_DISC = PropertyBool.create("has_disc");

    public MusicRecorder() {
        super(Material.WOOD, MapColor.DIRT);
        setHardness(1F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(HAS_RECORD, false).withProperty(HAS_DISC, false));
        setHarvestLevel("axe", 3);
        setResistance(1F);
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, IBlockState state,
                                    @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (state.getValue(HAS_RECORD)) {
            this.dropRecord(worldIn, pos);
            state = state.withProperty(HAS_RECORD, false);
            worldIn.setBlockState(pos, state, 2);
            return true;
        } else if(state.getValue(HAS_DISC)) {
            this.dropRecord(worldIn, pos);
            state = state.withProperty(HAS_DISC, false);
            worldIn.setBlockState(pos, state, 2);
            return true;
        } else return false;
    }

    public void insertRecord(World worldIn, BlockPos pos, IBlockState state, ItemStack recordStack, UUID uuid) {
        EventsCommon.recordWorld.put(pos,worldIn);
        EventsCommon.recordHolder.put(pos, recordStack.copy());
        EventsCommon.recordUUID.put(pos, uuid);
        EventsCommon.tickCounter.put(pos, 0);
        if(recordStack.getItem() instanceof BlankRecord) worldIn.setBlockState(pos, state.withProperty(HAS_RECORD, Boolean.TRUE), 2);
        else worldIn.setBlockState(pos, state.withProperty(HAS_DISC, Boolean.TRUE), 2);
    }

    private void dropRecord(World worldIn, BlockPos pos) {
        if (!worldIn.isRemote) {
            EventsCommon.recordHolder.putIfAbsent(pos,ItemStack.EMPTY);
            ItemStack itemstack = EventsCommon.recordHolder.get(pos);
            if (!itemstack.isEmpty()) {
                EventsCommon.recordHolder.put(pos,ItemStack.EMPTY);
                double d0 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                double d1 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
                double d2 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                ItemStack itemstack1 = itemstack.copy();
                EntityItem entityitem = new EntityItem(worldIn, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, itemstack1);
                entityitem.setDefaultPickupDelay();
                worldIn.spawnEntity(entityitem);
            }
        }
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        this.dropRecord(worldIn, pos);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state,
                                          float chance, int fortune) {
        if (!worldIn.isRemote)
            super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);
    }

    //TODO Stop using depreciated methods
    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(@Nonnull IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        ItemStack itemstack = EventsCommon.recordHolder.get(pos);
        if (!itemstack.isEmpty())
            return 15;
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(HAS_RECORD, meta > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HAS_RECORD) ? 1 : 0;
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HAS_RECORD, HAS_DISC);
    }
}
