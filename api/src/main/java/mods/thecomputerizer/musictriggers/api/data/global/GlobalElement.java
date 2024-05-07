package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;

public abstract class GlobalElement extends ParameterWrapper {
    
    protected GlobalElement(String name) {
        super(name);
    }
    
    @Override protected Class<? extends ParameterWrapper> getTypeClass() {
        return GlobalElement.class;
    }
    
    @Override protected String getTypeName() {
        return "Global";
    }
}