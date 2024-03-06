package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;

public class ParameterString extends Parameter<String> {

    public ParameterString(String defaultValue) {
        super(defaultValue);
    }

    public ParameterString(ByteBuf buf) {
        super(buf);
    }

    @Override
    protected String read(ByteBuf buf) {
        return NetworkHelper.readString(buf);
    }

    @Override
    protected void parseValueInner(String unparsed) {
        setValue(unparsed);
    }

    @Override
    protected void write(ByteBuf buf, String val) {
        NetworkHelper.writeString(buf,val);
    }
}
