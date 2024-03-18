package mods.thecomputerizer.musictriggers.api.data.parameter;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UniversalParameters extends ParameterWrapper {

    public static UniversalParameters get(ChannelAPI channel, String type, Consumer<Map<String,Parameter<?>>> parameterSettings) {
        return new UniversalParameters(channel,type,parameterSettings);
    }

    private final String type;
    private final Consumer<Map<String,Parameter<?>>> parameterSettings;

    public UniversalParameters(ChannelAPI channel, String type, Consumer<Map<String,Parameter<?>>> parameterSettings) {
        super(channel);
        this.type = type;
        this.parameterSettings = parameterSettings;
    }

    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return UniversalParameters.class;
    }

    @Override
    protected String getTypeName() {
        return "Universal Parameters ["+this.type+"]";
    }

    @Override
    protected Map<String,Parameter<?>> initParameterMap() {
        Map<String,Parameter<?>> map = new HashMap<>();
        this.parameterSettings.accept(map);
        return map;
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {}

    @Override
    public boolean isResource() {
        return false;
    }

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }
}
