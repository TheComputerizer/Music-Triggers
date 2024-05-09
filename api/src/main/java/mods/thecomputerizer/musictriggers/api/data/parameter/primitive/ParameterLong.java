package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterLong extends ParameterNumber<Long> {

    public ParameterLong(long defaultValue) {
        super(defaultValue);
    }
    
    @SuppressWarnings("unused") //See ParameterHelper#parse
    public ParameterLong(ByteBuf buf) {
        super(buf);
    }

    @Override
    protected Long read(ByteBuf buf) {
        return buf.readLong();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0L;
        else if(value instanceof Boolean) this.value = (Boolean)value ? 1L : 0L;
        else if(value instanceof Number) this.value = ((Number)value).longValue();
        else this.value = Long.parseLong(value.toString());
    }
    
    @Override
    protected void write(ByteBuf buf, Long val) {
        buf.writeLong(val);
    }
}
