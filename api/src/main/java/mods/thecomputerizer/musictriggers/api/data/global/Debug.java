package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;

public class Debug extends GlobalElement { //TODO Implement log_level and max_hover_elements in the gui
    
    protected Debug() {
        super("Debug");
    }
    
    public void flipBooleanParameter(String name) {
        setParameterValue(name,!getParameterAsBoolean(name),getParameter(name));
    }
    
    @Override protected TableRef getReferenceData() {
        return MTDataRef.DEBUG;
    }
}
