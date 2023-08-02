package mods.thecomputerizer.musictriggers.server.data;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

@SuppressWarnings("ConstantValue")
public class PersistentTriggerDataProvider implements ICapabilitySerializable<CompoundNBT> {


    @CapabilityInject(IPersistentTriggerData.class)
    public static final Capability<IPersistentTriggerData> PERSISTANCE_TRIGGER_DATA = null;

    public static IPersistentTriggerData getPlayerCapability(ServerPlayerEntity player) {
        return player.getCapability(PERSISTANCE_TRIGGER_DATA).orElseGet(() -> null);
    }

    private final LazyOptional<IPersistentTriggerData> instance = LazyOptional.of(PERSISTANCE_TRIGGER_DATA::getDefaultInstance);

    @Override
    public <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        return cap == PERSISTANCE_TRIGGER_DATA ? this.instance.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return (CompoundNBT)PERSISTANCE_TRIGGER_DATA.getStorage().writeNBT(PERSISTANCE_TRIGGER_DATA,this.instance.orElse(null),null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        PERSISTANCE_TRIGGER_DATA.getStorage().readNBT(PERSISTANCE_TRIGGER_DATA,this.instance.orElse(null),null,nbt);
    }
}
