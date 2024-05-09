package mods.thecomputerizer.musictriggers.api.data.parameter;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterNumber;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.GenericUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unused")
public abstract class ParameterWrapper implements LoggableAPI {

    @Getter protected final String name;
    private final Map<String,Parameter<?>> parameters;
    @Setter @Getter UniversalParameters universals;
    
    protected ParameterWrapper(String name) {
        this(name,null);
    }
    
    protected ParameterWrapper(String name, TableRef ref) {
        this.name = name;
        this.parameters = Collections.unmodifiableMap(initParameterMap(Objects.nonNull(ref) ? ref : getReferenceData()));
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

    public @Nullable Parameter<?> getParameter(String name) {
        Parameter<?> parameter = this.parameters.get(name.equals("id") ? "identifier" : name);
        if(this instanceof UniversalParameters || Objects.isNull(this.universals)) return parameter;
        Parameter<?> universal = this.universals.getParameter(name);
        return Objects.isNull(parameter) || (Objects.nonNull(universal) && parameter.isDefault()) ? universal : parameter;
    }

    public boolean getParameterAsBoolean(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterBoolean) return ((ParameterBoolean)parameter).getValue();
        if(parameter instanceof ParameterNumber<?>) return ((ParameterNumber<?>)parameter).doubleValue()!=0d;
        return Objects.nonNull(parameter) && Boolean.parseBoolean(parameter.getValue().toString());
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
        return ((ParameterList<?>)parameter).getValue();
    }

    public Number getParameterAsNumber(String name) {
        Parameter<?> parameter = getParameter(name);
        return parameter instanceof ParameterNumber<?> ?
                (Number)parameter.getValue() : getParameterAsNumber(parameter,name);
    }

    /**
     * Assumes the parameter has already been verified to not be an instance of ParameterNumber
     */
    protected Number getParameterAsNumber(Parameter<?> parameter, String name) {
        if(parameter instanceof ParameterString) {
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
        return Objects.nonNull(parameter) ? parameter.getValue().toString() : null;
    }
    
    protected abstract TableRef getReferenceData();
    public abstract Class<? extends ParameterWrapper> getTypeClass();
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
    
    protected void inheritParameters(ParameterWrapper wrapper) {
        for(Entry<String,Parameter<?>> entry : this.parameters.entrySet()) {
            Parameter<?> other = wrapper.getParameter(entry.getKey());
            if(Objects.nonNull(other)) entry.getValue().setValue(other.getValue());
        }
    }

    protected Map<String,Parameter<?>> initParameterMap(TableRef table) {
        Map<String,Parameter<?>> map = new HashMap<>();
        if(Objects.nonNull(table))
            for(ParameterRef<?> ref : table.getParameters())
                addParameter(map,ref.getName(),ref.toParameter());
        initExtraParameters(map);
        return map;
    }

    protected @Nullable Parameter<?> initParameter(String parameter, Parameter<?> defaultParameter) {
        return defaultParameter;
    }
    
    protected void initExtraParameters(Map<String,Parameter<?>> map) {}
    
    @Override
    public void logAll(String message, Object ... args) {
        MTLogger.logAll(getTypeName(),getName(),message,args);
    }
    
    @Override
    public void logDebug(String message, Object ... args) {
        MTLogger.logDebug(getTypeName(),getName(),message,args);
    }
    
    @Override
    public void logError(String message, Object ... args) {
        MTLogger.logError(getTypeName(),getName(),message,args);
    }
    
    @Override
    public void logFatal(String message, Object ... args) {
        MTLogger.logFatal(getTypeName(),getName(),message,args);
    }
    
    @Override
    public void logInfo(String message, Object ... args) {
        MTLogger.logInfo(getTypeName(),getName(),message,args);
    }

    protected void logMissingParameter(String name) {
        logError("Missing required `{}` parameter!",name);
    }

    protected void logMissingParameters(String ... names) {
        logError("Missing a 1 or more required parameters from {}! (All of these are required)",TextHelper.arrayToString(", ",(Object[])names));
    }

    protected void logMissingPotentialParameter(String ... names) {
        logError("Missing a required parameter from {}! (Only 1 of these is required)",TextHelper.arrayToString(", ",(Object[])names));
    }
    
    @Override
    public void logTrace(String message, Object ... args) {
        MTLogger.logTrace(getTypeName(),getName(),message,args);
    }
    
    @Override
    public void logWarn(String message, Object ... args) {
        MTLogger.logWarn(getTypeName(),getName(),message,args);
    }

    public boolean matchesAll(ParameterWrapper wrapper) {
        for(Entry<String,Parameter<?>> entry : this.parameters.entrySet())
            if(!GenericUtils.matches(entry.getValue(),wrapper.getParameter(entry.getKey()))) return false;
        return true;
    }

    public boolean parse(Toml table) {
        for(Entry<String,Parameter<?>> entry : this.parameters.entrySet()) {
            String name = entry.getKey();
            if(table.hasEntry(name)) {
                Parameter<?> parameter = entry.getValue();
                setParameterValue(name,table.getValue(name),parameter);
            }
        }
        return verifyRequiredParameters();
    }
    
    public boolean parseTriggers(ChannelHelper helper, String channel, Collection<TriggerAPI> triggers) {
        return parseTriggers(helper,channel,triggers,"triggers");
    }
    
    public boolean parseTriggers(ChannelHelper helper, String channel, Collection<TriggerAPI> triggers, String parameterName) {
        return parseTriggers(helper.findChannel(this,channel),triggers,parameterName);
    }
    
    public boolean parseTriggers(ChannelAPI channel, Collection<TriggerAPI> triggers) {
        return parseTriggers(channel,triggers,"triggers");
    }
    
    public boolean parseTriggers(ChannelAPI channel, Collection<TriggerAPI> triggers, String parameterName) {
        return parseTriggers(channel,triggers,getParameterAsList(parameterName));
    }
    
    protected boolean parseTriggers(ChannelAPI channel, Collection<TriggerAPI> triggers, Collection<?> triggerRefs) {
        if(!TriggerHelper.findTriggers(channel,triggers,triggerRefs)) {
            logError("Failed to parse 1 or more triggers!");
            return false;
        }
        if(triggers.isEmpty()) logDebug("No triggers were found! Attempting to load anyways");
        return true;
    }

    protected <T> void setParameterValue(String name, T value, @Nullable Parameter<?> parameter) {
        if(Objects.nonNull(parameter)) parameter.setValue(value);
        else logWarn("Cannot set value for paramenter `{}` that does not exist in {}!",name,getTypeName());
    }
    
    @Override
    public String toString() {
        return getTypeName()+"["+getName()+"]";
    }
    
    public boolean verifyRequiredParameters() {
        return true;
    }
}