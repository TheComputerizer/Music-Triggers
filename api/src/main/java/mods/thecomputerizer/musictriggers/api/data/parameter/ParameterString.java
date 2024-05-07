package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;

import javax.annotation.Nullable;
import java.util.Objects;

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
    public void setValue(@Nullable Object value) {
        this.value = Objects.nonNull(value) ? value.toString() : null;
    }
    
    @Override
    protected void write(ByteBuf buf, String val) {
        NetworkHelper.writeString(buf,val);
    }
}
