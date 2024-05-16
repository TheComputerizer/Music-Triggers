package mods.thecomputerizer.musictriggers.api.data.nbt;

import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;

public interface NBTLoadable {
    
    boolean hasDataToSave();
    void onConnected(CompoundTagAPI<?> worldData);
    void onLoaded(CompoundTagAPI<?> globalData);
    void saveGlobalTo(CompoundTagAPI<?> globalData);
    void saveWorldTo(CompoundTagAPI<?> worldData);
}