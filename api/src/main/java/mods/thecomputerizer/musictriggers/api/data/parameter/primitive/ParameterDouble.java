package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterDouble extends ParameterNumber<Double> {

    public ParameterDouble(double defaultValue) {
        super(defaultValue);
    }
    
    @SuppressWarnings("unused") //See ParameterHelper#parse
    public ParameterDouble(ByteBuf buf) {
        super(buf);
    }
    
    @Override public ParameterDouble copy() {
        ParameterDouble copy = new ParameterDouble(this.defaultValue);
        copy.value = this.value;
        return copy;
    }
    
    @Override public boolean isByte() {
        return false;
    }
    
    @Override public boolean isDouble() {
        return true;
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
        return false;
    }

    @Override
    protected Double read(ByteBuf buf) {
        return buf.readDouble();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0d;
        else if(value instanceof Boolean) this.value = (Boolean)value ? 1d : 0d;
        else if(value instanceof Number) this.value = ((Number)value).doubleValue();
        else {
            try {
                this.value = Double.parseDouble(value.toString());
            } catch(NumberFormatException ignored) {}
        }
    }
    
    @Override
    protected void write(ByteBuf buf, Double val) {
        buf.writeDouble(val);
    }
}
