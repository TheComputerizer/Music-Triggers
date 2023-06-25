package mods.thecomputerizer.musictriggers.server.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class PersistentTriggerDataProvider implements ICapabilitySerializable<NBTTagCompound> {

    @CapabilityInject(IPersistentTriggerData.class)
    public static final Capability<IPersistentTriggerData> PERSISTANCE_TRIGGER_DATA = null;
    private final IPersistentTriggerData impl = PERSISTANCE_TRIGGER_DATA.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability== PERSISTANCE_TRIGGER_DATA;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == PERSISTANCE_TRIGGER_DATA ? PERSISTANCE_TRIGGER_DATA.cast(this.impl) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) PERSISTANCE_TRIGGER_DATA.getStorage().writeNBT(PERSISTANCE_TRIGGER_DATA,this.impl,null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        PERSISTANCE_TRIGGER_DATA.getStorage().readNBT(PERSISTANCE_TRIGGER_DATA, this.impl, null, nbt);
    }
}
