package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.IndexFinder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

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

    @Override
    public void writeDefault(Holder holder) {
        Table table = holder.addTable(null,"registration");
        appendToTable(holder,table);
        holder.andBlank(1,new IndexFinder(table,1));
    }
}
