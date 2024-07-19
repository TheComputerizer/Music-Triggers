package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

public class BasicLink extends DataLink {
    
    public BasicLink() {
        super(false);
    }
    
    @Override public TextAPI<?> getDescription() {
        return null;
    }
    
    @Override public TextAPI<?> getDisplayName() {
        return null;
    }
    
    @Override public void populateToml(Toml toml) {}
}