package mods.thecomputerizer.musictriggers.api.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class FileRemapper {
    
    private final Map<String,Collection<String>> tableMappings;
    
    protected FileRemapper() {
        this.tableMappings = initTableMappings(new HashMap<>());
    }
    
    protected abstract Map<String,Collection<String>> initTableMappings(Map<String,Collection<String>> map);
    
    public String getMappedName(String key) {
        for(Entry<String,Collection<String>> entry : this.tableMappings.entrySet())
            if(entry.getValue().contains(key)) return entry.getKey();
        return key;
    }
}