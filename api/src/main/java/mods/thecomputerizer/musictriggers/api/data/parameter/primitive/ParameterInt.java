package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterInt extends ParameterNumber<Integer> {

    public ParameterInt(int defaultValue) {
        super(defaultValue);
    }
    
    @SuppressWarnings("unused") //See ParameterHelper#parse
    public ParameterInt(ByteBuf buf) {
        super(buf);
    }
    
    @Override public ParameterInt copy() {
        ParameterInt copy = new ParameterInt(this.defaultValue);
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
        return true;
    }
    
    @Override public boolean isLong() {
        return false;
    }
    
    @Override public boolean isShort() {
        return false;
    }

    @Override protected Integer read(ByteBuf buf) {
        return buf.readInt();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0;
        else if(value instanceof Boolean) this.value = (Boolean)value ? 1 : 0;
        else if(value instanceof Number) this.value = ((Number)value).intValue();
        else {
            try {
                this.value = Integer.parseInt(value.toString());
            } catch(NumberFormatException ignored) {}
        }
    }
    
    @Override protected void write(ByteBuf buf, Integer val) {
        buf.writeInt(val);
    }
}
