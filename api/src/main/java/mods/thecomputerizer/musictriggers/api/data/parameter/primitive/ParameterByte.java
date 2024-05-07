package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterByte extends ParameterNumber<Byte> {

    public ParameterByte(byte defaultValue) {
        super(defaultValue);
    }

    public ParameterByte(ByteBuf buf) {
        super(buf);
    }

    @Override
    protected Byte read(ByteBuf buf) {
        return buf.readByte();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0;
        else if(value instanceof Boolean) this.value = (Boolean)value ? (byte)1 : 0;
        else if(value instanceof Number) this.value = ((Number)value).byteValue();
        else this.value = Byte.parseByte(value.toString());
    }
    
    @Override
    protected void write(ByteBuf buf, Byte val) {
        buf.writeByte(val);
    }
}
