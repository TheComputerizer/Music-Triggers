package mods.thecomputerizer.musictriggers.registry.blocks;

import mods.thecomputerizer.musictriggers.registry.ItemRegistry;
import mods.thecomputerizer.musictriggers.registry.TileRegistry;
import mods.thecomputerizer.musictriggers.registry.tiles.MusicRecorderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class MusicRecorder extends BaseEntityBlock {

    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;
    public static final BooleanProperty HAS_DISC = BooleanProperty.create("has_disc");

    public MusicRecorder(BlockBehaviour.Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.FALSE).setValue(HAS_DISC, Boolean.FALSE));
    }


    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult ray) {
        if (state.getValue(HAS_RECORD)) {
            this.dropRecord(world, pos);
            state = state.setValue(HAS_RECORD, false);
            world.setBlock(pos, state, 2);
            return InteractionResult.sidedSuccess(world.isClientSide);
        } else if(state.getValue(HAS_DISC)) {
            this.dropRecord(world, pos);
            state = state.setValue(HAS_DISC, false);
            world.setBlock(pos, state, 2);
            return InteractionResult.sidedSuccess(world.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    public void insertRecord(Level world, BlockPos pos, ItemStack recordStack, BlockState state, Player player) {
        if (!world.isClientSide) {
            if (recordStack.getItem()==ItemRegistry.BLANK_RECORD || recordStack.getItem()==ItemRegistry.MUSIC_TRIGGERS_RECORD) {
                boolean wasBlank = recordStack.getItem()==ItemRegistry.BLANK_RECORD;
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof MusicRecorderEntity recorderEntity) {
                    if (recorderEntity.isEmpty()) {
                        recorderEntity.insertRecord(recordStack, player);
                        world.setBlock(pos,state.setValue(HAS_RECORD,wasBlank).setValue(HAS_DISC,!wasBlank),2);
                    }
                }
            }
        }
    }

    public void dropRecord(Level world, BlockPos pos) {
        if (!world.isClientSide) {
            BlockEntity entity = world.getBlockEntity(pos);
            if(entity instanceof MusicRecorderEntity recorderEntity) {
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
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState state2, boolean b) {
        if(!state.is(state2.getBlock())) {
            this.dropRecord(world, pos);
            super.onRemove(state,world,pos,state2,b);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal (BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);
        return entity instanceof MusicRecorderEntity && !((MusicRecorderEntity)entity).isEmpty() ? 15 : 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(HAS_RECORD).add(HAS_DISC);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MusicRecorderEntity(pos,state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTicker(level, type, TileRegistry.MUSIC_RECORDER_ENTITY);
    }

    @Nullable
    private static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockEntityType<T> serverType,
                                                                                      BlockEntityType<MusicRecorderEntity> clientType) {
        return level.isClientSide ? null : createTickerHelper(serverType, clientType, MusicRecorderEntity::serverTick);
    }
}
