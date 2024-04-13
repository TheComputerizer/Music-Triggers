package mods.thecomputerizer.musictriggers.api.data.parameter;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterNumber;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import javax.annotation.Nullable;
import java.util.*;

public abstract class ParameterWrapper extends ChannelElement {

    private final Map<String,Parameter<?>> parameters;

    protected ParameterWrapper(ChannelAPI channel) {
        super(channel);
        this.parameters = Collections.unmodifiableMap(initParameterMap());
    }

    protected void addParameter(Map<String,Parameter<?>> map, String name, @Nullable Parameter<?> parameter) {
        parameter = initParameter(name,parameter);
        if(Objects.nonNull(parameter)) map.put(name,parameter);
    }

    protected Map<String,Object> asValueMap() {
        Map<String,Object> map = new HashMap<>();
        for(Map.Entry<String,Parameter<?>> entry : this.parameters.entrySet())
            map.put(entry.getKey(),entry.getValue().getValue());
        return map;
    }

    @Override
    public void close() {
        this.parameters.clear();
    }

    public @Nullable Parameter<?> getParameter(String name) {
        Parameter<?> parameter = this.parameters.get(name.equals("id") ? "identifier" : name);
        if(Objects.nonNull(parameter)) {
            if(parameter.isDefault() && !(this instanceof UniversalParameters)) {
                UniversalParameters universals = this.channel.getData().getUniversals(getTypeClass());
                if(Objects.nonNull(universals)) {
                    Parameter<?> universal = universals.getParameter(name);
                    if(Objects.nonNull(universal)) return universal;
                }
            }
        }
        return parameter;
    }

    public boolean getParameterAsBoolean(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterBoolean) return ((ParameterBoolean)parameter).getValue();
        logWarn("Attempting to access non boolean parameter `{}` as a boolean! Things might not get parsed "+
                "correctly",name);
        if(parameter instanceof ParameterNumber<?>) return ((ParameterNumber<?>)parameter).doubleValue()!=0d;
        return Boolean.parseBoolean(parameter.getValue().toString());
    }

    public byte getParameterAsByte(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterNumber<?>) return ((ParameterNumber<?>)parameter).byteValue();
        return getParameterAsNumber(parameter,name).byteValue();
    }

    public double getParameterAsDouble(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterNumber<?>) return ((ParameterNumber<?>)parameter).doubleValue();
        return getParameterAsNumber(parameter,name).doubleValue();
    }

    public float getParameterAsFloat(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterNumber<?>) return ((ParameterNumber<?>)parameter).floatValue();
        return getParameterAsNumber(parameter,name).floatValue();
    }

    public int getParameterAsInt(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterNumber<?>) return ((ParameterNumber<?>)parameter).intValue();
        return getParameterAsNumber(parameter,name).intValue();
    }

    public long getParameterAsLong(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterNumber<?>) return ((ParameterNumber<?>)parameter).longValue();
        return getParameterAsNumber(parameter,name).longValue();
    }

    public short getParameterAsShort(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterNumber<?>) return ((ParameterNumber<?>)parameter).shortValue();
        return getParameterAsNumber(parameter,name).shortValue();
    }

    public List<?> getParameterAsList(String name) {
        Parameter<?> parameter = getParameter(name);
        if(Objects.isNull(parameter)) {
            logError("Unable to access list parameter `{}` that does not exist!",name);
            return Collections.emptyList();
        }
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
        return getParameterAsNumber(parameter,name);
    }

    /**
     * Assumes the parameter has already been verified to not be an instance of ParameterNumber
     */
    protected Number getParameterAsNumber(Parameter<?> parameter, String name) {
        if(parameter instanceof ParameterString) {
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

    protected abstract Class<? extends ChannelElement> getTypeClass();

    protected abstract String getTypeName();

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

    protected abstract Map<String,Parameter<?>> initParameterMap();

    protected @Nullable Parameter<?> initParameter(String parameter, Parameter<?> defaultParameter) {
        return defaultParameter;
    }

    protected abstract void initExtraParameters(Map<String,Parameter<?>> map);

    protected void logMissingParameter(String name) {
        logError("{} is missing a required `{}` parameter!",getTypeName(),name);
    }

    protected void logMissingParameters(String ... names) {
        logError("{} is missing a required `{}` parameter! (All of these are required)",getTypeName(),names);
    }

    protected void logMissingPotentialParameter(String ... names) {
        logError("{} is missing a required `{}` parameter! (Only 1 of these is required)",getTypeName(),names);
    }

    public boolean matchesAll(ParameterWrapper wrapper) {
        for(Map.Entry<String,Parameter<?>> entry : this.parameters.entrySet()) {
            String name = entry.getKey();
            Parameter<?> parameter = entry.getValue();
            if(wrapper.hasParameter(name) && parameter.getValue().toString().equals(wrapper.getParameter(name).getValue().toString()))
                continue;
            return false;
        }
        return true;
    }

    protected boolean parseParameters(Table table) {
        for(Map.Entry<String,Parameter<?>> entry : this.parameters.entrySet()) {
            String name = entry.getKey();
            if(table.hasVar(name)) {
                Parameter<?> parameter = entry.getValue();
                setParameterValue(name,table.getValOrDefault(name,parameter.getDefaultValue()),parameter);
            }
        }
        return verifyRequiredParameters();
    }

    @SuppressWarnings("unchecked")
    protected <T> void setParameterValue(String name, T value, @Nullable Parameter<?> parameter) {
        if(Objects.nonNull(parameter)) ((Parameter<T>)parameter).setValue(value);
        else logWarn("Cannot set value for paramenter `{}` that does not exist in {}!",name,getTypeName());
    }

    public abstract boolean verifyRequiredParameters();
}