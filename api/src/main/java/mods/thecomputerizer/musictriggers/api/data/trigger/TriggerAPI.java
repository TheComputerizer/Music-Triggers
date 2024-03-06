package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.ChannelData;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterNumber;

import javax.annotation.Nullable;
import java.util.*;

public abstract class TriggerAPI extends ChannelData {

    private final Map<String,Parameter<?>> parameters;
    @Getter private final String name;
    @Getter @Setter private boolean enabled;

    protected TriggerAPI(ChannelAPI channel, String name) {
        super(channel);
        this.parameters = initParameterMap();
        this.name = name;
    }

    protected void addParameter(Map<String,Parameter<?>> map, String name, @Nullable Parameter<?> parameter) {
        parameter = initParameter(name,parameter);
        if(Objects.nonNull(parameter)) map.put(name,parameter);
    }

    public abstract String getNameWithID();

    public @Nullable Parameter<?> getParameter(String name) {
        return this.parameters.get(name.equals("id") ? "identifier" : name);
    }

    public List<?> getParameterAsList(String name) {
        Parameter<?> parameter = getParameter(name);
        if(!(parameter instanceof ParameterList<?>)) {
            logWarn("Attempting to access non list parameter `{}` as a list! A singleton list will be "+
                    "substitured, but things may break!",name);
            return Collections.singletonList(parameter.getValue());
        }
        return ((ParameterList<?>)parameter).getValues();
    }

    public Number getParameterAsNumber(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterNumber<?>) return (Number)parameter.getValue();
        else if(parameter instanceof ParameterString) {
            logWarn("Attempting to access string parameter `{}` as a number! The type will be assumed to "+
                    "be double",name);
            String value = parameter.getValue().toString();
            try {
                return Double.parseDouble(value);
            } catch(NumberFormatException ex) {
                logError("Failed to parse parameter `{}` with value `{}` as double!",name,value);
                return 0;
            }
        }
        logError("Unable to get parameter `{}` as a number!",name);
        return 0;
    }

    public String getParameterAsString(String name) {
        Parameter<?> parameter = getParameter(name);
        return parameter.getValue().toString();
    }

    public List<String> getRequiredMods() {
        return Collections.emptyList();
    }

    public boolean hasAllNonDefaultParameter(String ... names) {
        for(String name : names)
            if(!hasNonDefaultParameter(name)) return false;
        return true;
    }

    public boolean hasAllParameters(String ... names) {
        for(String name : names)
            if(!hasParameter(name)) return false;
        return true;
    }

    public boolean hasAnyNonDefaultParameter(String ... names) {
        for(String name : names)
            if(hasNonDefaultParameter(name)) return true;
        return false;
    }

    public boolean hasAnyParameter(String ... names) {
        for(String name : names)
            if(hasParameter(name)) return true;
        return false;
    }

    public boolean hasNonDefaultParameter(String name) {
        Parameter<?> parameter = getParameter(name);
        return Objects.nonNull(parameter) && !parameter.isDefault();
    }

    public boolean hasParameter(String name) {
        return Objects.nonNull(getParameter(name));
    }

    protected Map<String,Parameter<?>> initParameterMap() {
        Map<String,Parameter<?>> map = new HashMap<>();
        addParameter(map,"fade_in",new ParameterInt(0));
        addParameter(map,"fade_out",new ParameterInt(0));
        addParameter(map,"max_tracks",new ParameterInt(0));
        addParameter(map,"not",new ParameterBoolean(false));
        addParameter(map,"passive_persistence",new ParameterBoolean(false));
        addParameter(map,"persistence",new ParameterInt(0));
        addParameter(map,"priority",new ParameterInt(0));
        addParameter(map,"song_delay",new ParameterInt(0));
        addParameter(map,"start_delay",new ParameterInt(0));
        addParameter(map,"start_toggled",new ParameterBoolean(true));
        addParameter(map,"stop_delay",new ParameterInt(0));
        addParameter(map,"toggle_inactive_playable",new ParameterBoolean(false));
        addParameter(map,"toggle_save_status",new ParameterInt(0));
        addParameter(map,"trigger_delay",new ParameterInt(0));
        initExtraParameters(map);
        return map;
    }

    protected @Nullable Parameter<?> initParameter(String parameter, Parameter<?> defaultParameter) {
        return defaultParameter;
    }

    protected abstract void initExtraParameters(Map<String,Parameter<?>> map);

    public abstract boolean isActive();

    public boolean isServer() {
        return false;
    }

    protected void logMissingParameter(String name) {
        logError("Trigger `{}` is missing a required `{}` parameter!",getName(),name);
    }

    protected void logMissingParameters(String ... names) {
        logError("Trigger `{}` is missing a required `{}` parameter! (All of these are required)",getName(),names);
    }

    protected void logMissingPotentialParameter(String ... names) {
        logError("Trigger `{}` is missing a required `{}` parameter! (Only 1 of these is required)",getName(),names);
    }

    public void onConnect() {

    }

    public void onDisconnect() {

    }

    protected abstract boolean verifyRequiredParameters();
}