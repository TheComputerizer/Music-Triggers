package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterFloat extends ParameterNumber<Float> {

    public ParameterFloat(float defaultValue) {
        super(defaultValue);
    }

    public ParameterFloat(ByteBuf buf) {
        super(buf);
    }

    @Override
    protected Float read(ByteBuf buf) {
        return buf.readFloat();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0f;
        else if(value instanceof Boolean) this.value = (Boolean)value ? 1f : 0f;
        else if(value instanceof Number) this.value = ((Number)value).floatValue();
        else this.value = Float.parseFloat(value.toString());
    }
    
    @Override
    protected void write(ByteBuf buf, Float val) {
        buf.writeFloat(val);
    }
}
