package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ParameterLong extends ParameterNumber<Long> {

    public ParameterLong(long defaultValue) {
        super(defaultValue);
    }

    public ParameterLong(ByteBuf buf) {
        super(buf);
    }

    @Override
    public void appendToTable(Holder holder, Table table, String name) {
        holder.addVariable(table,name,getValue());
    }

    @Override
    protected Long read(ByteBuf buf) {
        return buf.readLong();
    }

    @Override
    protected void parseValueInner(String unparsed) {
        this.value = Long.parseLong(unparsed);
    }

    @Override
    protected void write(ByteBuf buf, Long val) {
        buf.writeLong(val);
    }
}
