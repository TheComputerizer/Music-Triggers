package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterByte extends ParameterNumber<Byte> {

    public ParameterByte(byte defaultValue) {
        super(defaultValue);
    }
    
    @SuppressWarnings("unused") //See ParameterHelper#parse
    public ParameterByte(ByteBuf buf) {
        super(buf);
    }
    
    @Override public ParameterByte copy() {
        ParameterByte copy = new ParameterByte(this.defaultValue);
        copy.value = this.value;
        return copy;
    }
    
    @Override public boolean isByte() {
        return true;
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
        return false;
    }
    
    @Override
    protected Byte read(ByteBuf buf) {
        return buf.readByte();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0;
        else if(value instanceof Boolean) this.value = (Boolean)value ? (byte)1 : 0;
        else if(value instanceof Number) this.value = ((Number)value).byteValue();
        else {
            try {
                this.value = Byte.parseByte(value.toString());
            } catch(NumberFormatException ignored) {}
        }
    }
    
    @Override
    protected void write(ByteBuf buf, Byte val) {
        buf.writeByte(val);
    }
}
