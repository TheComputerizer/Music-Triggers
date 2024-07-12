package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.GenericUtils;

import javax.annotation.Nullable;

@Getter
public abstract class Parameter<T> {

    protected final T defaultValue;
    protected T value;

    protected Parameter(T defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    protected Parameter(ByteBuf buf) {
        this.defaultValue = read(buf);
        this.value = read(buf);
    }
    
    public abstract Parameter<T> copy();
    
    @Override
    public boolean equals(Object other) {
        return other instanceof Parameter<?> && GenericUtils.matches(this.value,((Parameter<?>)other).value);
    }
    
    public abstract boolean isBool();
    public abstract boolean isByte();

    public boolean isDefault() {
        return GenericUtils.matches(this.value,this.defaultValue);
    }
    
    public abstract boolean isDouble();
    public abstract boolean isFloat();
    public abstract boolean isInt();
    public abstract boolean isList();
    public abstract boolean isLong();
    public abstract boolean isNumber();
    public abstract boolean isPrimitive();
    public abstract boolean isShort();
    public abstract boolean isString();

    protected abstract T read(ByteBuf buf);
    public abstract void setValue(@Nullable Object value);
    
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    public void write(ByteBuf buf) {
        NetworkHelper.writeString(buf,getClass().getName());
        write(buf,this.defaultValue);
        write(buf,this.value);
    }

    protected abstract void write(ByteBuf buf, T val);
}