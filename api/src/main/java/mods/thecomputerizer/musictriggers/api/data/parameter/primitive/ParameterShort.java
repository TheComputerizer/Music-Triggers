package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterShort extends ParameterNumber<Short> {

    public ParameterShort(short defaultValue) {
        super(defaultValue);
    }

    public ParameterShort(ByteBuf buf) {
        super(buf);
    }

    @Override
    protected Short read(ByteBuf buf) {
        return buf.readShort();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0;
        else if(value instanceof Boolean) this.value = (Boolean)value ? (short)1 : 0;
        else if(value instanceof Number) this.value = ((Number)value).shortValue();
        else this.value = Short.parseShort(value.toString());
    }
    
    @Override
    protected void write(ByteBuf buf, Short val) {
        buf.writeLong(val);
    }
}
