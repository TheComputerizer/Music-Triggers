package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;

@Getter
public abstract class Parameter<T> {

    protected final T defaultValue;
    @Setter protected T value;

    protected Parameter(T defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    protected Parameter(ByteBuf buf) {
        this.defaultValue = read(buf);
        this.value = read(buf);
    }
    protected abstract T read(ByteBuf buf);

    public void parseValue(String unparsed) {
        parseValueInner(unparsed);
    }

    protected abstract void parseValueInner(String unparsed);

    public void write(ByteBuf buf) {
        NetworkHelper.writeString(buf,getClass().getName());
        write(buf,this.defaultValue);
        write(buf,this.value);
    }
    protected abstract void write(ByteBuf buf, T val);
}