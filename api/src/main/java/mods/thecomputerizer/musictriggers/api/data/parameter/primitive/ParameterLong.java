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
    
    @Override public ParameterLong copy() {
        ParameterLong copy = new ParameterLong(this.defaultValue);
        copy.value = this.value;
        return copy;
    }
    
    @Override public boolean isByte() {
        return false;
    }
    
    @Override public boolean isDouble() {
        return false;
    }
    
    @Override public boolean isFloat() {
        return false;
    }
    
    @Override public boolean isInt() {
        return false;
    }
    
    @Override public boolean isLong() {
        return true;
    }
    
    @Override public boolean isShort() {
        return false;
    }

    @Override protected Long read(ByteBuf buf) {
        return buf.readLong();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0L;
        else if(value instanceof Boolean) this.value = (Boolean)value ? 1L : 0L;
        else if(value instanceof Number) this.value = ((Number)value).longValue();
        else {
            try {
                this.value = Long.parseLong(value.toString());
            } catch(NumberFormatException ignored) {}
        }
    }
    
    @Override protected void write(ByteBuf buf, Long val) {
        buf.writeLong(val);
    }
}
