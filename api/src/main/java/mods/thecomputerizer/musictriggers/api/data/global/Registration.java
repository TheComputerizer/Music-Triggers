package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;

import java.util.Map;

public class Registration extends GlobalElement {

    @Override
    public String getTypeName() {
        return "Registration";
    }

    @Override
    protected void supplyParameters(Map<String,Parameter<?>> map) {
        map.put("CLIENT_SIDE_ONLY",new ParameterBoolean(false));
        map.put("REGISTER_DISCS",new ParameterBoolean(true));
    }

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }
}
