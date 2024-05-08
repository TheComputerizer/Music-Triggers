package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import lombok.Setter;

public class LoadTracker {
    
    @Setter private boolean resourcesLoaded;
    @Getter private boolean loading = true;
    @Getter private boolean client;
    @Getter private boolean config;
    
    public boolean areResourcesLoaded() {
        return this.resourcesLoaded;
    }
    
    public void finishLoading() {
        this.loading = false;
    }
    
    public void queueReload(boolean client, boolean config) {
        this.loading = true;
        this.client = client;
        this.config = config;
    }
}