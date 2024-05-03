package mods.thecomputerizer.musictriggers.api.config;

import java.util.HashSet;
import java.util.Set;

public abstract class ConfigRemapper {
    
    private final ConfigVersion target;
    private final Set<FileRemapper> remappers;
    
    protected ConfigRemapper(ConfigVersion target) {
        this.remappers = addRemappers(new HashSet<>());
        this.target = target;
    }
    
    protected abstract Set<FileRemapper> addRemappers(Set<FileRemapper> set);
    
    public void apply(ConfigVersion from) {
    
    }
    
    protected abstract void write();
}