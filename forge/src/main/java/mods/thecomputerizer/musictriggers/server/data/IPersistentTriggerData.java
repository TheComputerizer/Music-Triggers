package mods.thecomputerizer.musictriggers.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface IPersistentTriggerData {

    void of(PersistentTriggerData data);
    void onLogin(ServerPlayer player);
    void writePreferredSort(int preferredSort);
    void initChannel(String channel);
    void writeToggleStatus(String channel, String trigger, boolean isToggled);
    void setAudioPlayed(String channel, String audio, List<String> audioTriggers, int timesPlayed);
    void clearChannelData(String channel);
    void clearAllData();
    CompoundTag writeToNBT();
    void readFromNBT(CompoundTag tag);
}
