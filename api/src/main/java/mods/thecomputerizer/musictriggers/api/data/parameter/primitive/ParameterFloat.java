package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterFloat extends ParameterNumber<Float> {

    public ParameterFloat(float defaultValue) {
        super(defaultValue);
    }
    
    @SuppressWarnings("unused") //See ParameterHelper#parse
    public ParameterFloat(ByteBuf buf) {
        super(buf);
    }
    
    @Override public ParameterFloat copy() {
        ParameterFloat copy = new ParameterFloat(this.defaultValue);
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
        return true;
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
    protected Float read(ByteBuf buf) {
        return buf.readFloat();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0f;
        else if(value instanceof Boolean) this.value = (Boolean)value ? 1f : 0f;
        else if(value instanceof Number) this.value = ((Number)value).floatValue();
        else {
            try {
                this.value = Float.parseFloat(value.toString());
            } catch(NumberFormatException ignored) {}
        }
    }
    
    @Override
    protected void write(ByteBuf buf, Float val) {
        buf.writeFloat(val);
    }
}
