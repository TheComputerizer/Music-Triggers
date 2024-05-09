package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import lombok.Setter;

@Setter
public class LoadTracker {
    
    private boolean resourcesLoaded;
    @Getter private boolean loading = true;
    @Getter private boolean client;
    @Getter private boolean connected;
    
    public boolean areResourcesLoaded() {
        return this.resourcesLoaded;
    }
}