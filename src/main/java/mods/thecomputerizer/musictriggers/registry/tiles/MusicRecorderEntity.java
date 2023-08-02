package mods.thecomputerizer.musictriggers.registry.tiles;

import mods.thecomputerizer.musictriggers.registry.BlockRegistry;
import mods.thecomputerizer.musictriggers.registry.TileRegistry;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import mods.thecomputerizer.musictriggers.server.channels.ServerTriggerStatus;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

import java.util.Random;

public class MusicRecorderEntity extends TileEntity implements ITickableTileEntity {

    private final Random random;
    private PlayerEntity player;
    private ItemStack record = ItemStack.EMPTY;
    private int counter;

    public MusicRecorderEntity() {
        this(TileRegistry.MUSIC_RECORDER_ENTITY.get());
    }

    private MusicRecorderEntity(TileEntityType<?> type) {
        super(type);
        this.random = new Random();
        this.counter = 0;
    }

    public boolean isEmpty() {
        return this.record == ItemStack.EMPTY;
    }

    public void insertRecord(ItemStack stack, PlayerEntity player) {
        this.record = stack.copy();
        stack.setCount(0);
        this.counter = this.random.nextInt(600);
        this.player = player;
    }

    @Override
    public void tick() {
        if(this.counter>0) {
            this.counter--;
            if(this.counter<=0) record();
        }
    }

    private void record() {
        ItemStack stack = ServerTriggerStatus.recordAudioData(this.player.getUUID(),this.record);
        if(!stack.isEmpty()) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if(state.getBlock()==BlockRegistry.MUSIC_RECORDER.get()) {
                this.record = stack;
                this.level.playSound(null,this.worldPosition, SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundCategory.BLOCKS, 1f,1f);
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
