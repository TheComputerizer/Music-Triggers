package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.LoggableAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterNumber;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Variable;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.*;

public abstract class GlobalElement implements LoggableAPI {

    private final Map<String,Parameter<?>> parameters;

    protected GlobalElement() {
        this.parameters = initParameters();
    }

    /**
     * Used for top level parameters that are not stored in a table by default
     */
    protected Table constructTable(Holder holder) {
        Table table = new Table(1,null,1,getTypeName());
        for(Map.Entry<String,Parameter<?>> entry : this.parameters.entrySet()) {
            String name = entry.getKey();
            Parameter<?> parameter = entry.getValue();
            Optional<Variable> var = holder.getVar(name);
            var.ifPresent(v -> parameter.parseValue(v.get().toString()));
            table.addItem(parameter.asTomlVar(table,name));
        }
        return table;
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
        ChannelAPI.log("Global",getTypeName(),Level.ALL,msg,args);
    }

    @Override
    public void logDebug(String msg, Object ... args) {
        ChannelAPI.log("Global",getTypeName(),Level.DEBUG,msg,args);
    }

    @Override
    public void logError(String msg, Object ... args) {
        ChannelAPI.log("Global",getTypeName(),Level.ERROR,msg,args);
    }

    @Override
    public void logFatal(String msg, Object ... args) {
        ChannelAPI.log("Global",getTypeName(),Level.FATAL,msg,args);
    }

    @Override
    public void logInfo(String msg, Object ... args) {
        ChannelAPI.log("Global",getTypeName(),Level.INFO,msg,args);
    }

    @Override
    public void logTrace(String msg, Object ... args) {
        ChannelAPI.log("Global",getTypeName(),Level.TRACE,msg,args);
    }

    @Override
    public void logWarn(String msg, Object ... args) {
        ChannelAPI.log("Global",getTypeName(),Level.WARN,msg,args);
    }

    public abstract boolean parse(Table table);
    public abstract boolean parse(Holder holder);

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