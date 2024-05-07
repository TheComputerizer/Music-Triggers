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
    
    @Override
    public boolean equals(Object other) {
        return other instanceof Parameter<?> && GenericUtils.matches(this.value,((Parameter<?>)other).value);
    }

    public boolean isDefault() {
        return GenericUtils.matches(this.value,this.defaultValue);
    }

    protected abstract T read(ByteBuf buf);
    public abstract void setValue(@Nullable Object value);
    
    @Override
    public String toString() {
        return this.value+" (default="+this.defaultValue+")";
    }

    public void write(ByteBuf buf) {
        NetworkHelper.writeString(buf,getClass().getName());
        write(buf,this.defaultValue);
        write(buf,this.value);
    }

    protected abstract void write(ByteBuf buf, T val);
}