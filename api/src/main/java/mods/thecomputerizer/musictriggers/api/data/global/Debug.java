package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;

import java.util.Map;

public class Debug extends GlobalElement { //TODO Implement log_level and max_hover_elements in the gui
    
    public void flipBooleanParameter(String name) {
        setExistingParameterValue(name,!getParameterAsBoolean(name));
    }

    @Override
    public String getTypeName() {
        return "Debug";
    }

    @Override
    protected void supplyParameters(Map<String,Parameter<?>> map) {
        for(ParameterRef<?> ref : MTDataRef.DEBUG.getParameters()) map.put(ref.getName(),ref.toParameter());
    }

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }
}
