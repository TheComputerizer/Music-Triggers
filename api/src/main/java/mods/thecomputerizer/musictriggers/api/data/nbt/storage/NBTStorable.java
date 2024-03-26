package mods.thecomputerizer.musictriggers.api.data.nbt.storage;

import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;

public interface NBTStorable {

    void readFrom(CompoundTagAPI tag);
    CompoundTagAPI writeTo();
}