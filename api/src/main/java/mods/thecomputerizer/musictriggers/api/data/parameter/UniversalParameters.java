package mods.thecomputerizer.musictriggers.api.data.parameter;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UniversalParameters extends ParameterWrapper {

    public static UniversalParameters get(ChannelAPI channel, String type, Consumer<Map<String,Parameter<?>>> parameterSettings) {
        return new UniversalParameters(channel,type,parameterSettings);
    }

    private final String type;

    public UniversalParameters(ChannelAPI channel, String type, Consumer<Map<String,Parameter<?>>> parameterSettings) {
        super(channel,parameterSettings);
        this.type = type;
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
       return new HashMap<>();
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {}

    @Override
    public boolean isResource() {
        return false;
    }

    @Override
    public boolean parseParameters(Toml table) {
        return super.parseParameters(table);
    }

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }
    
    @Override
    public void close() {}
}
