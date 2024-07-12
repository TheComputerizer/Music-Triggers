package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;

public abstract class DataLink {
    
    protected final MTScreenInfo type;
    protected final boolean dual;
    
    protected DataLink(MTScreenInfo type, boolean dual) {
        this.type = type;
        this.dual = dual;
    }
}
