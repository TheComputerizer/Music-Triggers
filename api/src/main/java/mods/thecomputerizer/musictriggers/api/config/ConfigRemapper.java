package mods.thecomputerizer.musictriggers.api.config;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlRemapper;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class ConfigRemapper extends TomlRemapper {
    
    final TableRef ref;
    
    public ConfigRemapper(TableRef ref) {
        this.ref = ref;
    }
    
    public abstract TomlRemapper getNextRemapper(TableRef next);
    
    @Nullable @Override public TomlRemapper getNextRemapper(String table) {
        TableRef next = this.ref.findChild(table);
        return Objects.nonNull(next) ? getNextRemapper(next) : null;
    }
    
    @Override public String remapTable(String name) {
        return name;
    }
    
    @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
        return entry;
    }
}
