package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterShort extends ParameterNumber<Short> {

    public ParameterShort(short defaultValue) {
        super(defaultValue);
    }
    
    @SuppressWarnings("unused") //See ParameterHelper#parse
    public ParameterShort(ByteBuf buf) {
        super(buf);
    }
    
    @Override public ParameterShort copy() {
        ParameterShort copy = new ParameterShort(this.defaultValue);
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
        return false;
    }
    
    @Override public boolean isShort() {
        return true;
    }

    @Override
    protected Short read(ByteBuf buf) {
        return buf.readShort();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0;
        else if(value instanceof Boolean) this.value = (Boolean)value ? (short)1 : 0;
        else if(value instanceof Number) this.value = ((Number)value).shortValue();
        else {
            try {
                this.value = Short.parseShort(value.toString());
            } catch(NumberFormatException ignored) {}
        }
    }
    
    @Override
    protected void write(ByteBuf buf, Short val) {
        buf.writeLong(val);
    }
}
