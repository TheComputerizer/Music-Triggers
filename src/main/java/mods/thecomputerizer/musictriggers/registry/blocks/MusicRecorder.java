package mods.thecomputerizer.musictriggers.registry.blocks;

import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.registry.tiles.MusicRecorderEntity;
import net.minecraft.block.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@SuppressWarnings({"NullableProblems", "deprecation"})
public class MusicRecorder extends Block implements ITileEntityProvider {

    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;
    public static final BooleanProperty HAS_DISC = BooleanProperty.create("has_disc");

    public MusicRecorder(AbstractBlock.Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.FALSE).setValue(HAS_DISC, Boolean.FALSE));
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (state.getValue(HAS_RECORD)) {
            this.dropRecord(world, pos);
            state = state.setValue(HAS_RECORD, false);
            world.setBlock(pos, state, 2);
            return ActionResultType.sidedSuccess(world.isClientSide);
        } else if(state.getValue(HAS_DISC)) {
            this.dropRecord(world, pos);
            state = state.setValue(HAS_DISC, false);
            world.setBlock(pos, state, 2);
            return ActionResultType.sidedSuccess(world.isClientSide);
        } else {
            return ActionResultType.PASS;
        }
    }

    public void insertRecord(World world, BlockPos pos, ItemStack recordStack, BlockState state, PlayerEntity player) {
        if (!world.isClientSide) {
            if (recordStack.getItem()==ItemRegistry.BLANK_RECORD.get() || recordStack.getItem()==ItemRegistry.MUSIC_TRIGGERS_RECORD.get()) {
                boolean wasBlank = recordStack.getItem()==ItemRegistry.BLANK_RECORD.get();
                TileEntity entity = world.getBlockEntity(pos);
                if (entity instanceof MusicRecorderEntity) {
                    MusicRecorderEntity recorderEntity = (MusicRecorderEntity) entity;
                    if (recorderEntity.isEmpty()) {
                        recorderEntity.insertRecord(recordStack, player);
                        world.setBlock(pos,state.setValue(HAS_RECORD,wasBlank).setValue(HAS_DISC,!wasBlank),2);
                    }
                }
            }
        }
    }

    public void dropRecord(World world, BlockPos pos) {
        if (!world.isClientSide) {
            TileEntity entity = world.getBlockEntity(pos);
            if(entity instanceof MusicRecorderEntity) {
                MusicRecorderEntity recorderEntity = (MusicRecorderEntity)entity;
                if(!recorderEntity.isEmpty()) {
                    double d0 = (double)(world.random.nextFloat() * 0.7F) + (double)0.15F;
                    double d1 = (double)(world.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
                    double d2 = (double)(world.random.nextFloat() * 0.7F) + (double)0.15F;
                    ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + d0,
                            (double) pos.getY() + d1, (double) pos.getZ() + d2, recorderEntity.setDropped());
                    itemEntity.setDefaultPickUpDelay();
                    world.addFreshEntity(itemEntity);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean b) {
        if(!oldState.is(newState.getBlock())) {
            this.dropRecord(world, pos);
            super.onRemove(oldState,world,pos,newState,b);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
        TileEntity entity = world.getBlockEntity(pos);
        return entity instanceof MusicRecorderEntity && !((MusicRecorderEntity)entity).isEmpty() ? 15 : 0;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }


    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HAS_RECORD).add(HAS_DISC);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader reader) {
        return new MusicRecorderEntity();
    }
}
