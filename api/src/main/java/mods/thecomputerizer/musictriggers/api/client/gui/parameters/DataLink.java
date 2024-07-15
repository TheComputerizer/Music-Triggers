package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

public abstract class DataLink {
    
    protected final MTScreenInfo type;
    protected final boolean dual;
    @Getter protected boolean modified;
    
    protected DataLink(MTScreenInfo type, boolean dual) {
        this.type = type;
        this.dual = dual;
    }
    
    public String getTypeName() {
        return this.type.getType();
    }
    
    public abstract void populateToml(Toml toml);
    
    public void setModified(boolean modified) {
        if(modified) this.type.enableApplyButton();
        this.modified = modified;
    }
}
