package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ParameterByte extends ParameterNumber<Byte> {

    public ParameterByte(byte defaultValue) {
        super(defaultValue);
    }

    public ParameterByte(ByteBuf buf) {
        super(buf);
    }

    @Override
    public void appendToTable(Holder holder, Table table, String name) {
        holder.addVariable(table,name,getValue());
    }

    @Override
    protected Byte read(ByteBuf buf) {
        return buf.readByte();
    }

    @Override
    protected void parseValueInner(String unparsed) {
        this.value = Byte.parseByte(unparsed);
    }

    @Override
    protected void write(ByteBuf buf, Byte val) {
        buf.writeByte(val);
    }
}
