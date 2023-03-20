package mods.thecomputerizer.musictriggers.registry.blocks;

import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.registry.tiles.MusicRecorderEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
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

    public void insertRecord(World worldIn, BlockPos pos, ItemStack recordStack, EntityPlayer player) {
        if (!worldIn.isRemote) {
            if (recordStack.getItem()== ItemRegistry.BLANK_RECORD || recordStack.getItem()== ItemRegistry.MUSIC_TRIGGERS_RECORD) {
                int meta = recordStack.getItem()== ItemRegistry.BLANK_RECORD ? 1 : 2;
                TileEntity entity = worldIn.getTileEntity(pos);
                if (entity instanceof MusicRecorderEntity) {
                    MusicRecorderEntity recorderEntity = (MusicRecorderEntity) entity;
                    if (recorderEntity.isEmpty()) {
                        recorderEntity.validate();
                        recorderEntity.insertRecord(recordStack, player);
                        set(worldIn,pos,meta);
                    }
                }
            }
        }
    }

    public void dropRecord(World worldIn, BlockPos pos, boolean broken) {
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
                if(!broken) set(worldIn,pos,0);
            }
        }
    }

    private void set(World world, BlockPos pos, int meta) {
        IBlockState state = getStateFromMeta(meta);
        TileEntity tile = world.getTileEntity(pos);
        world.setBlockState(pos,state);
        if(tile instanceof MusicRecorderEntity) {
            tile.validate();
            world.setTileEntity(pos,tile);
        }
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        this.dropRecord(worldIn, pos, true);
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
        return this.getDefaultState().withProperty(HAS_RECORD, meta==1).withProperty(HAS_DISC, meta==2);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HAS_RECORD) ? 1 : state.getValue(HAS_DISC) ? 2 : 0;
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
