package mods.thecomputerizer.musictriggers.server.data;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class PersistentTriggerDataStorage implements Capability.IStorage<IPersistentTriggerData> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IPersistentTriggerData> capability, IPersistentTriggerData instance, EnumFacing side) {
        return instance.writeToNBT();
    }

    @Override
    public void readNBT(Capability<IPersistentTriggerData> capability, IPersistentTriggerData instance, EnumFacing side, NBTBase nbt) {
        instance.readFromNBT((NBTTagCompound)nbt);
    }
}
