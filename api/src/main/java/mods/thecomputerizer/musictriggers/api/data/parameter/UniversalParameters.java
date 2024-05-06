package mods.thecomputerizer.musictriggers.api.data.parameter;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.HashMap;
import java.util.Map;

public class UniversalParameters extends ParameterWrapper {

    public static UniversalParameters get(ChannelAPI channel, String type) {
        return new UniversalParameters(channel,type);
    }

    private final String type;

    public UniversalParameters(ChannelAPI channel, String type) {
        super(channel,"Universal "+type);
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
        Map<String,Parameter<?>> map = new HashMap<>();
        TableRef table = "Triggers".equals(this.type) ? MTDataRef.UNIVERSAL_TRIGGERS : MTDataRef.UNIVERSAL_AUDIO;
        for(ParameterRef<?> ref : table.getParameters()) addParameter(map,ref.getName(),ref.toParameter());
        return map;
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
