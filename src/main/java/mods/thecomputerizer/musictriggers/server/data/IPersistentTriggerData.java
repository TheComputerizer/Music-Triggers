package mods.thecomputerizer.musictriggers.server.data;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public interface IPersistentTriggerData {

    void onLogin(EntityPlayerMP player);
    void writePreferredSort(int preferredSort);
    void initChannel(String channel);
    void writeToggleStatus(String channel, String trigger, boolean isToggled);
    void setAudioPlayed(String channel, String audio, List<String> audioTriggers);
    void clearChannelData(String channel);
    void clearAllData();
    NBTTagCompound writeToNBT();
    void readFromNBT(NBTTagCompound tag);
}
