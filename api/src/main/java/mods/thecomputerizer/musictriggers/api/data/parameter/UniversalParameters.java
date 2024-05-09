package mods.thecomputerizer.musictriggers.api.data.parameter;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;

public class UniversalParameters extends ChannelElement {

    public static UniversalParameters get(ChannelAPI channel, TableRef ref) {
        return new UniversalParameters(channel,ref);
    }

    private final TableRef ref;

    public UniversalParameters(ChannelAPI channel, TableRef ref) {
        super(channel,ref.getName(),ref);
        this.ref = ref;
    }
    
    @Override
    public void close() {}
    
    @Override protected TableRef getReferenceData() {
        return this.ref;
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
}
