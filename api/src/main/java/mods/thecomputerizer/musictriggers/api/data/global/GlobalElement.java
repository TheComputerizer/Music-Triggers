package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterNumber;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import javax.annotation.Nullable;
import java.util.*;

public abstract class GlobalElement implements LoggableAPI {

    private final Map<String,Parameter<?>> parameters;

    protected GlobalElement() {
        this.parameters = initParameters();
    }

    public @Nullable Parameter<?> getParameter(String name) {
        return this.parameters.get(name.equals("id") ? "identifier" : name);
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

    public abstract String getTypeName();

    private Map<String,Parameter<?>> initParameters() {
        Map<String,Parameter<?>> map = new HashMap<>();
        supplyParameters(map);
        return map;
    }

    protected abstract void supplyParameters(Map<String,Parameter<?>> map);

    @Override
    public void logAll(String msg, Object ... args) {
        MTLogger.logAll("Global",getTypeName(),msg,args);
    }

    @Override
    public void logDebug(String msg, Object ... args) {
        MTLogger.logDebug("Global",getTypeName(),msg,args);
    }

    @Override
    public void logError(String msg, Object ... args) {
        MTLogger.logError("Global",getTypeName(),msg,args);
    }

    @Override
    public void logFatal(String msg, Object ... args) {
        MTLogger.logFatal("Global",getTypeName(),msg,args);
    }

    @Override
    public void logInfo(String msg, Object ... args) {
        MTLogger.logInfo("Global",getTypeName(),msg,args);
    }

    @Override
    public void logTrace(String msg, Object ... args) {
        MTLogger.logTrace("Global",getTypeName(),msg,args);
    }

    @Override
    public void logWarn(String msg, Object ... args) {
        MTLogger.logWarn("Global",getTypeName(),msg,args);
    }

    public boolean parse(Toml table) {
        for(Map.Entry<String,Parameter<?>> entry : this.parameters.entrySet()) {
            String name = entry.getKey();
            if(table.hasEntry(name)) setParameterValue(name,table.getValue(name),entry.getValue());
        }
        return verifyRequiredParameters();
    }

    @SuppressWarnings("unchecked")
    protected <T> void setParameterValue(String name, T value, @Nullable Parameter<?> parameter) {
        if(Objects.nonNull(parameter)) {
            if(parameter instanceof ParameterList<?>) parameter.setListValue((List<?>)value);
            else ((Parameter<T>)parameter).setValue(value);
        } else logWarn("Cannot set value for paramenter `{}` that does not exist in {}!",name,getTypeName());
    }

    public abstract boolean verifyRequiredParameters();
}