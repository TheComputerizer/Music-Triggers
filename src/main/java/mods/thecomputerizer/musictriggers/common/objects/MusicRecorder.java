package mods.thecomputerizer.musictriggers.common.objects;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MusicRecorder extends Block implements ITileEntityProvider {

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

    public void insertRecord(World worldIn, BlockPos pos, IBlockState state, ItemStack recordStack, EntityPlayerMP player) {
        if(recordStack.getItem() instanceof BlankRecord || recordStack.getItem() instanceof MusicTriggersRecord) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if(entity instanceof MusicRecorderEntity) {
                MusicRecorderEntity recorderEntity = (MusicRecorderEntity)entity;
                if(recorderEntity.isEmpty()) {
                    recorderEntity.insertRecord(recordStack,player);
                    PropertyBool propertyBool = recordStack.getItem() instanceof BlankRecord ? HAS_RECORD : HAS_DISC;
                    worldIn.setBlockState(pos, state.withProperty(propertyBool, Boolean.TRUE), 2);
                }
            }
        }
    }

    public void dropRecord(World worldIn, BlockPos pos) {
        if (!worldIn.isRemote) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if(entity instanceof MusicRecorderEntity) {
                MusicRecorderEntity recorderEntity = (MusicRecorderEntity)entity;
                if(!recorderEntity.isEmpty()) {
                    double d0 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                    double d1 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
                    double d2 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                    EntityItem entityitem = new EntityItem(worldIn, (double) pos.getX() + d0,
                            (double) pos.getY() + d1, (double) pos.getZ() + d2, recorderEntity.setDropped());
                    entityitem.setDefaultPickupDelay();
                    worldIn.spawnEntity(entityitem);
                }
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
        TileEntity entity = worldIn.getTileEntity(pos);
        return entity instanceof MusicRecorderEntity && !((MusicRecorderEntity)entity).isEmpty() ? 15 : 0;
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

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new MusicRecorderEntity();
    }
}
