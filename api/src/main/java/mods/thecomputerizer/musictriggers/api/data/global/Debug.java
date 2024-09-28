package mods.thecomputerizer.musictriggers.api.data.global;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBool;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.DEBUG;

@Getter
public class Debug extends GlobalElement { //TODO Implement log_level and max_hover_elements in the gui
    
    private final Map<String,List<String>> formattedBlockedMods;
    
    protected Debug() {
        super("Debug");
        this.formattedBlockedMods = new HashMap<>();
    }
    
    public void flipBooleanParameter(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterBool) {
            ParameterBool bool = (ParameterBool)parameter;
            bool.setValue(!bool.getValue());
            logInfo("Successfully flipped debug parameter {} from {} to {}",name,!bool.getValue(),bool.getValue());
        }
    }
    
    @Override public TableRef getReferenceData() {
        return DEBUG;
    }
    
    public void setParameterValue(String name, Object value) {
        Parameter<?> parameter = getParameter(name);
        if(Objects.nonNull(parameter)) {
            Object old = parameter.getValue();
            setParameterValue(name,value,parameter);
            logInfo("Successfully set debug parameter {} from {} to {}",name,old,value);
        }
    }
    
    @Override public boolean parse(Toml table) {
        if(super.parse(table)) {
            this.formattedBlockedMods.clear();
            for(Object element : getParameterAsList("blocked_sound_categories")) {
                String pair = element.toString();
                int index = pair.indexOf(';');
                String mod = index==-1 ? "all" : pair.substring(0,index);
                String[] categories = (index==-1 ? pair : pair.substring(index+1)).split(",");
                if(categories.length>0) {
                    this.formattedBlockedMods.putIfAbsent(mod,new ArrayList<>());
                    this.formattedBlockedMods.get(mod).addAll(Arrays.asList(categories));
                }
            }
            return true;
        }
        return false;
    }
}
