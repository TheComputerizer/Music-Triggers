package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.common.MusicTriggersBlocks;
import mods.thecomputerizer.musictriggers.common.ServerData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;

import java.util.Random;

public class MusicRecorderEntity extends TileEntity implements ITickable {

    private final Random random;
    private EntityPlayerMP player;
    private ItemStack record = ItemStack.EMPTY;
    private int counter;

    public MusicRecorderEntity() {
        this.random = new Random();
        this.counter = 0;
    }

    public boolean isEmpty() {
        return this.record == ItemStack.EMPTY;
    }

    public void insertRecord(ItemStack recordStack, EntityPlayerMP player) {
        this.record = recordStack;
        this.counter = this.random.nextInt(5600);
        this.player = player;
    }

    @Override
    public void update() {
        if(this.counter>0) {
            this.counter--;
            if(this.counter<=0) record();
        }
    }

    private void record() {
        if(ServerData.recordAudioData(this.player.getUniqueID(),this.record)) {
            IBlockState state = this.world.getBlockState(this.pos);
            if(state.getBlock()==MusicTriggersBlocks.MUSIC_RECORDER) {
                this.world.playSound(null,this.pos, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.BLOCKS,
                        1f,1f);
                ((MusicRecorder) state.getBlock()).dropRecord(this.world, this.pos);
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
