package mods.thecomputerizer.musictriggers.api.data.parameter;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBool;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterNumber;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ArrayHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.iterator.IterableHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.GenericUtils;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

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
    
    public List<String> getBooleanParameterNames() {
        List<String> names = new ArrayList<>();
        for(Entry<String,Parameter<?>> entry : this.parameters.entrySet())
            if(entry.getValue() instanceof ParameterBool) names.add(entry.getKey());
        return names;
    }
    
    public Collection<DataLink> getChildWrappers(MTScreenInfo parent) {
        return Collections.emptySet();
    }
    
    public ParameterLink getLink() {
        return new ParameterLink(this,this.parameters);
    }
    
    protected abstract String getLogPrefix();

    public @Nullable Parameter<?> getParameter(String name) {
        Parameter<?> parameter = this.parameters.get(name.equals("id") ? "identifier" : name);
        if(this instanceof UniversalParameters || Objects.isNull(this.universals)) return parameter;
        Parameter<?> universal = this.universals.getParameter(name);
        return Objects.isNull(parameter) || (Objects.nonNull(universal) && parameter.isDefault()) ? universal : parameter;
    }

    public boolean getParameterAsBoolean(String name) {
        Parameter<?> parameter = getParameter(name);
        if(parameter instanceof ParameterBool) return ((ParameterBool)parameter).getValue();
        if(parameter instanceof ParameterNumber<?>) return ((ParameterNumber<?>)parameter).doubleValue()!=0d;
        return Objects.nonNull(parameter) && Boolean.parseBoolean(parameter.getValue().toString());
    }

    public byte getParameterAsByte(String name) {
        return getParameterAsNumber(getParameter(name),Number::byteValue,
                s -> RandomHelper.randomByte(name+"_as_number", s, (byte)0));
    }

    public double getParameterAsDouble(String name) {
        return getParameterAsNumber(getParameter(name),Number::doubleValue,
                s -> RandomHelper.randomDouble(name+"_as_number",s,0d));
    }

    public float getParameterAsFloat(String name) {
        return getParameterAsNumber(getParameter(name),Number::floatValue,
                s -> RandomHelper.randomFloat(name+"_as_number",s,0f));
    }

    public int getParameterAsInt(String name) {
        return getParameterAsNumber(getParameter(name),Number::intValue,
                s -> RandomHelper.randomInt(name+"_as_number",s,0));
    }

    public long getParameterAsLong(String name) {
        return getParameterAsNumber(getParameter(name),Number::longValue,
                s -> RandomHelper.randomLong(name+"_as_number",s,0L));
    }

    public short getParameterAsShort(String name) {
        return getParameterAsNumber(getParameter(name),Number::shortValue,
                s -> RandomHelper.randomShort(name+"_as_number",s,(short)0));
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
        return getParameterAsNumber(getParameter(name),n -> n,
                s -> RandomHelper.randomDouble(name+"_as_number",s,0d));
    }

    /**
     * Assumes the parameter has already been verified to not be an instance of ParameterNumber
     */
    protected <N extends Number> N getParameterAsNumber(
            Parameter<?> parameter, Function<Number,N> fromNumber, Function<String,N> fromString) {
        if(Objects.isNull(parameter)) return fromNumber.apply(0);
        if(parameter instanceof ParameterNumber) return fromNumber.apply(((ParameterNumber<?>)parameter).getValue());
        if(parameter instanceof ParameterString) return fromString.apply(((ParameterString)parameter).getValue());
        if(parameter instanceof ParameterBool) return fromNumber.apply(((ParameterBool)parameter).getValue() ? 1 : 0);
        return getValueAsNumber(parameter.getValue(),fromNumber,fromString);
    }

    public String getParameterAsString(String name) {
        return String.valueOf(getParameter(name));
    }
    
    protected <N extends Number> N getValueAsNumber(Object value, Function<Number,N> fromNumber, Function<String,N> fromString) {
        if(Objects.isNull(value)) return fromNumber.apply(0);
        if(value instanceof Number) return fromNumber.apply((Number)value);
        if(value instanceof String) return fromString.apply((String)value);
        if(value instanceof Boolean) return fromNumber.apply((Boolean)value ? 1 : 0);
        if(value instanceof Iterable<?>)
            return getValueAsNumber(IterableHelper.getElement(0,(Collection<?>)value),fromNumber,fromString);
        if(value instanceof Object[]) {
            Object[] array = (Object[])value;
            return getValueAsNumber(ArrayHelper.isNotEmpty(array) ? array[0] : 0,fromNumber,fromString);
        }
        return fromString.apply(String.valueOf(value));
    }
    
    public abstract TableRef getReferenceData();
    public abstract Class<? extends ParameterWrapper> getTypeClass();

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
    
    @Override public void logDebug(String message, Object ... args) {
        MTLogger.logDebug(getLogPrefix(), getName(), message, args);
    }
    
    @Override public void logError(String message, Object ... args) {
        MTLogger.logError(getLogPrefix(), getName(), message, args);
    }
    
    @Override public void logFatal(String message, Object ... args) {
        MTLogger.logFatal(getLogPrefix(), getName(), message, args);
    }
    
    @Override public void logInfo(String message, Object ... args) {
        MTLogger.logInfo(getLogPrefix(), getName(), message, args);
    }

    protected void logMissingParameter(String name) {
        logError("Missing required `{}` parameter!",name);
    }

    protected void logMissingParameters(String ... names) {
        logError("Missing 1 or more required parameters from [{}]! (All of these are required)",TextHelper.arrayToString(", ",(Object[])names));
    }

    protected void logMissingPotentialParameter(String ... names) {
        logError("Missing a required parameter from [{}]! (Only 1 of these is required)",TextHelper.arrayToString(", ",(Object[])names));
    }
    
    @Override public void logTrace(String message, Object ... args) {
        MTLogger.logTrace(getLogPrefix(), getName(), message, args);
    }
    
    @Override public void logWarn(String message, Object ... args) {
        MTLogger.logWarn(getLogPrefix(), getName(), message, args);
    }

    public boolean matchesAll(ParameterWrapper wrapper) {
        for(Entry<String,Parameter<?>> entry : this.parameters.entrySet())
            if(!GenericUtils.matches(entry.getValue(),wrapper.getParameter(entry.getKey()))) return false;
        return true;
    }

    public boolean parse(Toml table) {
        for(Entry<String,Parameter<?>> entry : this.parameters.entrySet()) {
            String name = entry.getKey();
            if(name.equals("identifier") && !table.hasEntry("identifier") && table.hasEntry("id")) {
                table.addEntry("identifier",table.getEntry("id").getValue());
                table.removeEntry("id");
            }
            if(table.hasEntry(name)) {
                Parameter<?> parameter = entry.getValue();
                setParameterValue(name,table.getValue(name),parameter);
            }
        }
        return verifyRequiredParameters();
    }
    
    public boolean parseTriggers(ChannelHelper helper, String channel, Collection<TriggerAPI> triggers) {
        return parseTriggers(true,helper,channel,triggers,"triggers");
    }
    
    public boolean parseTriggers(boolean implyMissing, ChannelHelper helper, String channel, Collection<TriggerAPI> triggers) {
        return parseTriggers(implyMissing,helper,channel,triggers,"triggers");
    }
    
    public boolean parseTriggers(ChannelHelper helper, String channel, Collection<TriggerAPI> triggers, String parameterName) {
        return parseTriggers(true,helper.findChannel(this,channel),triggers,parameterName);
    }
    
    public boolean parseTriggers(boolean implyMissing, ChannelHelper helper, String channel, Collection<TriggerAPI> triggers, String parameterName) {
        return parseTriggers(implyMissing,helper.findChannel(this,channel),triggers,parameterName);
    }
    
    public boolean parseTriggers(ChannelAPI channel, Collection<TriggerAPI> triggers) {
        return parseTriggers(true,channel,triggers,"triggers");
    }
    
    public boolean parseTriggers(boolean implyMissing, ChannelAPI channel, Collection<TriggerAPI> triggers) {
        return parseTriggers(implyMissing,channel,triggers,"triggers");
    }
    
    public boolean parseTriggers(ChannelAPI channel, Collection<TriggerAPI> triggers, String parameterName) {
        return parseTriggers(true,channel,triggers,getParameterAsList(parameterName));
    }
    
    public boolean parseTriggers(boolean implyMissing, ChannelAPI channel, Collection<TriggerAPI> triggers, String parameterName) {
        return parseTriggers(implyMissing,channel,triggers,getParameterAsList(parameterName));
    }
    
    protected boolean parseTriggers(ChannelAPI channel, Collection<TriggerAPI> triggers, Collection<?> triggerRefs) {
        return parseTriggers(true,channel,triggers,triggerRefs);
    }
    
    protected boolean parseTriggers(boolean implyMissing, ChannelAPI channel, Collection<TriggerAPI> triggers, Collection<?> triggerRefs) {
        if(!TriggerHelper.findTriggers(implyMissing,channel,triggers,triggerRefs)) {
            logError("Failed to parse 1 or more triggers!");
            return false;
        }
        if(triggers.isEmpty()) logDebug("No triggers were found! Attempting to load anyways");
        return true;
    }

    protected <T> void setParameterValue(String name, T value, @Nullable Parameter<?> parameter) {
        if(Objects.nonNull(parameter)) parameter.setValue(value);
        else logWarn("Cannot set value for paramenter `{}` that does not exist in {}!", name, getLogPrefix());
    }
    
    @Override public String toString() {
        return getLogPrefix()+"["+getName()+"]";
    }
    
    public Toml toToml() {
        Toml toml = Toml.getEmpty();
        for(Entry<String,Parameter<?>> entry : this.parameters.entrySet())
            toml.addEntry(entry.getKey(),entry.getValue().getValue());
        return toTomlExtra(toml);
    }
    
    protected Toml toTomlExtra(Toml toml) {
        return toml;
    }
    
    public boolean verifyRequiredParameters() {
        TableRef table = getReferenceData();
        if(Objects.isNull(table)) return true;
        for(Entry<String,Parameter<?>> entry : this.parameters.entrySet()) {
            String name = entry.getKey();
            ParameterRef<?> ref = table.findParameter(name);
            Object value = entry.getValue().getValue();
            if(Objects.isNull(ref) || ref.isValid(value)) continue;
            logError("`{}` is not a valid value for the parameter {}!",value,name);
            return false;
        }
        return true;
    }
}