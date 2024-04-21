package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ParameterShort extends ParameterNumber<Short> {

    public ParameterShort(short defaultValue) {
        super(defaultValue);
    }

    public ParameterShort(ByteBuf buf) {
        super(buf);
    }

    @Override
    public void appendToTable(Holder holder, Table table, String name) {
        holder.addVariable(table,name,getValue());
    }

    @Override
    protected Short read(ByteBuf buf) {
        return buf.readShort();
    }

    @Override
    protected void parseValueInner(String unparsed) {
        this.value = Short.parseShort(unparsed);
    }

    @Override
    protected void write(ByteBuf buf, Short val) {
        buf.writeLong(val);
    }
}
