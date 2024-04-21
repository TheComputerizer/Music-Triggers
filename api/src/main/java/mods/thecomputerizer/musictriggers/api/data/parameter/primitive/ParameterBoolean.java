package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ParameterBoolean extends Parameter<Boolean> {

    public ParameterBoolean(boolean defaultValue) {
        super(defaultValue);
    }

    public ParameterBoolean(ByteBuf buf) {
        super(buf);
    }

    @Override
    public void appendToTable(Holder holder, Table table, String name) {
        holder.addVariable(table,name,getValue());
    }

    @Override
    protected Boolean read(ByteBuf buf) {
        return buf.readBoolean();
    }

    @Override
    protected void parseValueInner(String unparsed) {
        this.value = Boolean.parseBoolean(unparsed);
    }

    @Override
    protected void write(ByteBuf buf, Boolean val) {
        buf.writeBoolean(val);
    }
}
