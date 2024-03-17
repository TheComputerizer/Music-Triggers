package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Variable;

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

    public Variable asTomlVar(Table table, String name) {
        return new Variable(table.getMaxIndex(false,true),table,name,getValue());
    }

    public boolean isDefault() {
        return this.value==this.defaultValue || this.value.toString().equals(this.defaultValue.toString());
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