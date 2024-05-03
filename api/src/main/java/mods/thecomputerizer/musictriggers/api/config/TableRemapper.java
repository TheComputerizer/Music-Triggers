package mods.thecomputerizer.musictriggers.api.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public abstract class TableRemapper {
    
    private final Map<String,Collection<String>> mappings;
    private final Map<String,Function<Object,Object>> transformers;
    
    protected TableRemapper() {
        this.mappings = initMappings(new HashMap<>());
        this.transformers = initTransformers(new HashMap<>());
    }
    
    public String getMappedKey(String key) {
        for(Entry<String,Collection<String>> entry : this.mappings.entrySet())
            if(entry.getValue().contains(key)) return entry.getKey();
        return key;
    }
    
    protected abstract Map<String,Collection<String>> initMappings(Map<String,Collection<String>> map);
    protected abstract Map<String,Function<Object,Object>> initTransformers(Map<String,Function<Object,Object>> map);
    
    public void remap(Map<String,Object> map) {
        Map<String,Object> copy = new HashMap<>();
        for(Entry<String,Object> entry : map.entrySet()) copy.put(getMappedKey(entry.getKey()),entry.getValue());
        map.clear();
        map.putAll(copy);
        map.replaceAll((key,val) -> this.transformers.containsKey(key) ? this.transformers.get(key).apply(val) : val);
    }
}