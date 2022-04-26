package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.common.EventsCommon;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

@SuppressWarnings("deprecation")
public class MusicRecorder extends Block {

    public static final BooleanProperty HAS_RECORD = Properties.HAS_RECORD;
    public static final BooleanProperty HAS_DISC = BooleanProperty.of("has_disc");

    public MusicRecorder(AbstractBlock.Settings s) {
        super(s);
        this.setDefaultState(this.stateManager.getDefaultState().with(HAS_RECORD, false).with(HAS_DISC, Boolean.FALSE));
    }

    public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockHitResult res) {
        if (state.get(HAS_RECORD)) {
            this.dropRecord(worldIn, pos);
            state = state.with(HAS_RECORD, false);
            worldIn.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            return ActionResult.success(worldIn.isClient);
        } else if(state.get(HAS_DISC)) {
            this.dropRecord(worldIn, pos);
            state = state.with(HAS_DISC, false);
            worldIn.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
            return ActionResult.success(worldIn.isClient);
        } else {
            return ActionResult.PASS;
        }
    }

    public void insertRecord(World worldIn, BlockPos pos, BlockState state, ItemStack recordStack, UUID uuid) {
        EventsCommon.recordWorld.put(pos,worldIn);
        EventsCommon.recordHolder.put(pos, recordStack.copy());
        EventsCommon.recordUUID.put(pos, uuid);
        EventsCommon.tickCounter.put(pos, 0);
        if(recordStack.getItem() instanceof BlankRecord) worldIn.setBlockState(pos, state.with(HAS_RECORD, true), Block.NOTIFY_LISTENERS);
        else worldIn.setBlockState(pos, state.with(HAS_DISC, Boolean.TRUE), Block.NOTIFY_LISTENERS);
    }

    private void dropRecord(World worldIn, BlockPos pos) {
        if (!worldIn.isClient) {
            EventsCommon.recordHolder.putIfAbsent(pos,ItemStack.EMPTY);
            ItemStack itemstack = EventsCommon.recordHolder.get(pos);
            if (!itemstack.isEmpty()) {
                EventsCommon.recordHolder.put(pos,ItemStack.EMPTY);
                double d0 = (double) (worldIn.random.nextFloat() * 0.7F) + 0.15000000596046448D;
                double d1 = (double) (worldIn.random.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
                double d2 = (double) (worldIn.random.nextFloat() * 0.7F) + 0.15000000596046448D;
                ItemStack itemstack1 = itemstack.copy();
                ItemEntity itemEntity = new ItemEntity(worldIn, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, itemstack1);
                itemEntity.setToDefaultPickupDelay();
                worldIn.spawnEntity(itemEntity);
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World worldIn, BlockPos pos, BlockState state2, boolean b) {
        if (state.isOf(state2.getBlock())) {
            return;
        }
        this.dropRecord(worldIn, pos);
        super.onStateReplaced(state,worldIn,pos,state2,b);
    }

    @Override
    public void onBreak (World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (state.get(HAS_RECORD)) {
            return 15;
        }
        return 0;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HAS_RECORD).add(HAS_DISC);
    }
}
