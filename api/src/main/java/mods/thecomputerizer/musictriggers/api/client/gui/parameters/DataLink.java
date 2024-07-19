package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

public abstract class DataLink {
    
    protected final boolean dual;
    @Getter @Setter protected MTScreenInfo type;
    @Getter protected boolean modified;
    
    protected DataLink(boolean dual) {
        this.dual = dual;
    }
    
    public abstract TextAPI<?> getDescription();
    
    public abstract TextAPI<?> getDisplayName();
    
    public String getTypeName() {
        return this.type.getType();
    }
    
    public abstract void populateToml(Toml toml);
    
    public void setModified(boolean modified) {
        if(modified) this.type.enableApplyButton();
        this.modified = modified;
    }
}
