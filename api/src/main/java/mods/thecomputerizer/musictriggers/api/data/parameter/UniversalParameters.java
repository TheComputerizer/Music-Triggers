package mods.thecomputerizer.musictriggers.api.data.parameter;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;

public class UniversalParameters extends ChannelElement {

    public static UniversalParameters get(ChannelAPI channel, String type) {
        return new UniversalParameters(channel,type);
    }

    private final String type;

    public UniversalParameters(ChannelAPI channel, String type) {
        super(channel,type);
        this.type = type;
    }
    
    @Override protected TableRef getReferenceData() {
        return "Triggers".equals(this.type) ? MTDataRef.UNIVERSAL_TRIGGERS : MTDataRef.UNIVERSAL_AUDIO;
    }
    
    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return UniversalParameters.class;
    }

    @Override
    protected String getSubTypeName() {
        return "Universal";
    }

    @Override
    public boolean isResource() {
        return false;
    }
    
    @Override
    public void close() {}
}
