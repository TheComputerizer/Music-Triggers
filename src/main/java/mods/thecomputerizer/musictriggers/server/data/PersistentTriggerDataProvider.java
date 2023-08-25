package mods.thecomputerizer.musictriggers.server.data;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.theimpossiblelibrary.util.file.DataUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PersistentTriggerDataProvider {

    private static final ResourceLocation REGISTRY_NAME = Constants.res("persistent_trigger_data");
    private static PersistentTriggerDataProvider INSTANCE;

    public static PersistentTriggerData getPlayerData(ServerPlayer player) {
        return INSTANCE.getOrCreate(player.getStringUUID());
    }

    public static PersistentTriggerDataProvider getInstance() {
        if(Objects.isNull(INSTANCE)) INSTANCE = new PersistentTriggerDataProvider();
        return INSTANCE;
    }

    private final Map<String,PersistentTriggerData> playerData;

    private PersistentTriggerDataProvider() {
        this.playerData = new HashMap<>();
    }

    private PersistentTriggerData getOrCreate(String playerUUID) {
        if(!this.playerData.containsKey(playerUUID)) this.playerData.put(playerUUID,new PersistentTriggerData());
        return this.playerData.get(playerUUID);
    }

    public void writeToNBT(String worldName) {
        CompoundTag tag = DataUtil.getWorldData(Constants.MODID,worldName);
        if(Objects.nonNull(tag)) {
            CompoundTag dataTag = new CompoundTag();
            int size = this.playerData.size();
            dataTag.putInt("size",size);
            int i = 0;
            for(Map.Entry<String,PersistentTriggerData> dataEntry : this.playerData.entrySet()) {
                dataTag.put(dataEntry.getKey(), dataEntry.getValue().writeToNBT());
                dataTag.putString("key_"+i,dataEntry.getKey());
                i++;
            }
            tag.put(REGISTRY_NAME.toString(),dataTag);
        }
    }

    public void readFromNBT(String worldName) throws IOException {
        CompoundTag tag = DataUtil.getWorldData(Constants.MODID,worldName);
        if(Objects.nonNull(tag)) {
            CompoundTag dataTag = DataUtil.getOrCreateCompound(tag,REGISTRY_NAME.toString());
            int size = dataTag.getInt("size");
            for(int i=0;i<size;i++) {
                String key = dataTag.getString("key_"+i);
                this.playerData.put(key,new PersistentTriggerData());
                this.playerData.get(key).readFromNBT(DataUtil.getOrCreateCompound(dataTag,key));
            }
        }
        DataUtil.writeWorldData(tag,Constants.MODID,worldName);
    }
}
