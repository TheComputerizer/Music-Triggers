package mods.thecomputerizer.musictriggers.server.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class PersistentTriggerDataStorage implements Capability.IStorage<IPersistentTriggerData> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<IPersistentTriggerData> capability, IPersistentTriggerData instance, Direction side) {
        return instance.writeToNBT();
    }

    @Override
    public void readNBT(Capability<IPersistentTriggerData> capability, IPersistentTriggerData instance, Direction side, INBT nbt) {
        if(nbt instanceof CompoundNBT) instance.readFromNBT((CompoundNBT)nbt);
    }
}
