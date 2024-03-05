package mods.thecomputerizer.musictriggers.server.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class PersistentTriggerDataProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<IPersistentTriggerData> PERSISTANCE_TRIGGER_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    public static IPersistentTriggerData getPlayerCapability(ServerPlayer player) {
        return player.getCapability(PERSISTANCE_TRIGGER_DATA).orElseGet(() -> null);
    }

    private final IPersistentTriggerData instance;

    public PersistentTriggerDataProvider() {
        this.instance = new PersistentTriggerData();
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        return cap == PERSISTANCE_TRIGGER_DATA ? LazyOptional.of(() -> instance).cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.writeToNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.readFromNBT(nbt);
    }
}
