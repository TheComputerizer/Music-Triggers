package mods.thecomputerizer.musictriggers.registry.tiles;

import mods.thecomputerizer.musictriggers.registry.BlockRegistry;
import mods.thecomputerizer.musictriggers.registry.TileRegistry;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import mods.thecomputerizer.musictriggers.server.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class MusicRecorderEntity extends BlockEntity {

    private final Random random;
    private Player player;
    private ItemStack record = ItemStack.EMPTY;
    private int counter;

    public MusicRecorderEntity(BlockPos pos, BlockState state) {
        this(TileRegistry.MUSIC_RECORDER_ENTITY.get(),pos,state);
    }

    private MusicRecorderEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type,pos,state);
        this.random = new Random();
        this.counter = 0;
    }

    public boolean isEmpty() {
        return this.record == ItemStack.EMPTY;
    }

    public void insertRecord(ItemStack recordStack, Player player) {
        this.record = recordStack.copy();
        recordStack.setCount(0);
        this.counter = this.random.nextInt(600);
        this.player = player;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MusicRecorderEntity entity) {
        if(entity.counter>0) {
            entity.counter--;
            if(entity.counter<=0) entity.record();
        }
    }

    private void record() {
        ItemStack stack = ServerData.recordAudioData(this.player.getUUID(),this.record);
        if(stack != ItemStack.EMPTY) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if(state.getBlock()== BlockRegistry.MUSIC_RECORDER.get()) {
                this.record = stack;
                this.level.playSound(null,this.worldPosition, SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.BLOCKS, 1f,1f);
                ((MusicRecorder)state.getBlock()).dropRecord(this.level, this.worldPosition);
                state.setValue(MusicRecorder.HAS_RECORD,false).setValue(MusicRecorder.HAS_DISC,false);
                this.level.setBlock(this.worldPosition, state, 2);
            }
        }
        else insertRecord(this.record,this.player);
    }

    public ItemStack setDropped() {
        ItemStack ret = this.record.copy();
        this.player = null;
        this.record = ItemStack.EMPTY;
        this.counter = 0;
        return ret;
    }
}
