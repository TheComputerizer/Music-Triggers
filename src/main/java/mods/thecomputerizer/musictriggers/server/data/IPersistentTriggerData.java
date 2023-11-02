package mods.thecomputerizer.musictriggers.server.data;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.List;

public interface IPersistentTriggerData {

    void of(PersistentTriggerData data);
    void onLogin(ServerPlayerEntity player);
    void writePreferredSort(int preferredSort);
    void initChannel(String channel);
    void writeToggleStatus(String channel, String trigger, boolean isToggled);
    void setAudioPlayed(String channel, String audio, List<String> audioTriggers, int timesPlayed);
    void clearChannelData(String channel);
    void clearAllData();
    CompoundNBT writeToNBT();
    void readFromNBT(CompoundNBT tag);
}
