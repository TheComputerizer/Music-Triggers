package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.common.EventsCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.UUID;

@SuppressWarnings({"NullableProblems", "deprecation"})
public class MusicRecorder extends Block {

    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;
    public static final BooleanProperty HAS_DISC = BooleanProperty.create("has_disc");

    public MusicRecorder(BlockBehaviour.Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.FALSE).setValue(HAS_DISC, Boolean.FALSE));
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult res) {
        if (state.getValue(HAS_RECORD)) {
            this.dropRecord(worldIn, pos);
            state = state.setValue(HAS_RECORD, false);
            worldIn.setBlock(pos, state, 2);
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        } else if(state.getValue(HAS_DISC)) {
            this.dropRecord(worldIn, pos);
            state = state.setValue(HAS_DISC, false);
            worldIn.setBlock(pos, state, 2);
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    public void insertRecord(Level worldIn, BlockPos pos, BlockState state, ItemStack recordStack, UUID uuid) {
        EventsCommon.recordWorld.put(pos,worldIn);
        EventsCommon.recordHolder.put(pos, recordStack.copy());
        EventsCommon.recordUUID.put(pos, uuid);
        EventsCommon.tickCounter.put(pos, 0);
        if(recordStack.getItem() instanceof BlankRecord) worldIn.setBlock(pos, state.setValue(HAS_RECORD, Boolean.TRUE), 2);
        else worldIn.setBlock(pos, state.setValue(HAS_DISC, Boolean.TRUE), 2);
    }

    private void dropRecord(Level worldIn, BlockPos pos) {
        if (!worldIn.isClientSide) {
            EventsCommon.recordHolder.putIfAbsent(pos,ItemStack.EMPTY);
            ItemStack itemstack = EventsCommon.recordHolder.get(pos);
            if (!itemstack.isEmpty()) {
                EventsCommon.recordHolder.put(pos,ItemStack.EMPTY);
                double d0 = (double) (worldIn.random.nextFloat() * 0.7F) + 0.15000000596046448D;
                double d1 = (double) (worldIn.random.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
                double d2 = (double) (worldIn.random.nextFloat() * 0.7F) + 0.15000000596046448D;
                ItemStack itemstack1 = itemstack.copy();
                ItemEntity itemEntity = new ItemEntity(worldIn, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, itemstack1);
                itemEntity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itemEntity);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState state2, boolean b) {
        if(!state.is(state2.getBlock())) {
            this.dropRecord(worldIn, pos);
            super.onRemove(state,worldIn,pos,state2,b);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        ItemStack itemstack = EventsCommon.recordHolder.get(pos);
        if (!itemstack.isEmpty()) {
            return 15;
        }
        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState p_54296_) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(HAS_RECORD).add(HAS_DISC);
    }
}
