package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterString extends Parameter<String> {

    public ParameterString(String defaultValue) {
        super(defaultValue);
    }
    
    @SuppressWarnings("unused") //See ParameterHelper#parse
    public ParameterString(ByteBuf buf) {
        super(buf);
    }
    
    @Override public ParameterString copy() {
        ParameterString copy = new ParameterString(this.defaultValue);
        copy.value = this.value;
        return copy;
    }
    
    @Override public boolean isBool() {
        return false;
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
    
    @Override public boolean isList() {
        return false;
    }
    
    @Override public boolean isLong() {
        return false;
    }
    
    @Override public boolean isNumber() {
        return false;
    }
    
    @Override public boolean isPrimitive() {
        return false;
    }
    
    @Override public boolean isShort() {
        return false;
    }
    
    @Override public boolean isString() {
        return true;
    }
    
    @Override
    protected String read(ByteBuf buf) {
        return NetworkHelper.readString(buf);
    }
    
    @Override
    public void setValue(@Nullable Object value) {
        this.value = Objects.nonNull(value) ? value.toString() : null;
    }
    
    @Override
    protected void write(ByteBuf buf, String val) {
        NetworkHelper.writeString(buf,val);
    }
}
