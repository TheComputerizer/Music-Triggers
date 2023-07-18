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
        setHardness(1f);
        this.setDefaultState(this.blockState.getBaseState().withProperty(HAS_RECORD, false).withProperty(HAS_DISC, false));
        setHarvestLevel("axe", 3);
        setResistance(1f);
    }

    public void insertRecord(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        if(!world.isRemote) {
            if(stack.getItem()==ItemRegistry.BLANK_RECORD || stack.getItem()==ItemRegistry.MUSIC_TRIGGERS_RECORD) {
                int meta = stack.getItem()==ItemRegistry.BLANK_RECORD ? 1 : 2;
                TileEntity entity = world.getTileEntity(pos);
                if(entity instanceof MusicRecorderEntity) {
                    MusicRecorderEntity recorderEntity = (MusicRecorderEntity) entity;
                    if(recorderEntity.isEmpty()) {
                        recorderEntity.validate();
                        recorderEntity.insertRecord(stack, player);
                        set(world,pos,meta);
                    }
                }
            }
        }
    }

    public void dropRecord(World world, BlockPos pos, boolean broken) {
        if (!world.isRemote) {
            TileEntity entity = world.getTileEntity(pos);
            if(entity instanceof MusicRecorderEntity) {
                MusicRecorderEntity recorderEntity = (MusicRecorderEntity)entity;
                if(!recorderEntity.isEmpty()) {
                    double d0 = (double) (world.rand.nextFloat() * 0.7f) + 0.15000000596046448d;
                    double d1 = (double) (world.rand.nextFloat() * 0.7f) + 0.06000000238418579d + 0.6d;
                    double d2 = (double) (world.rand.nextFloat() * 0.7f) + 0.15000000596046448d;
                    EntityItem entityitem = new EntityItem(world, (double) pos.getX() + d0,
                            (double) pos.getY() + d1, (double) pos.getZ() + d2, recorderEntity.setDropped());
                    entityitem.setDefaultPickupDelay();
                    world.spawnEntity(entityitem);
                }
                if(!broken) set(world,pos,0);
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
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        this.dropRecord(world, pos, true);
        super.breakBlock(world, pos, state);
    }

    @Override
    public void dropBlockAsItemWithChance(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state,
                                          float chance, int fortune) {
        if (!world.isRemote)
            super.dropBlockAsItemWithChance(world, pos, state, chance, 0);
    }

    //TODO Stop using depreciated methods
    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(@Nonnull IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity entity = world.getTileEntity(pos);
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
    public TileEntity createNewTileEntity(@Nonnull World world, int meta) {
        return new MusicRecorderEntity();
    }
}
