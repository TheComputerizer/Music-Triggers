package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ParameterInt extends ParameterNumber<Integer> {

    public ParameterInt(int defaultValue) {
        super(defaultValue);
    }

    public ParameterInt(ByteBuf buf) {
        super(buf);
    }

    @Override
    public void appendToTable(Holder holder, Table table, String name) {
        holder.addVariable(table,name,getValue());
    }

    @Override
    protected Integer read(ByteBuf buf) {
        return buf.readInt();
    }

    @Override
    protected void parseValueInner(String unparsed) {
        this.value = Integer.parseInt(unparsed);
    }

    @Override
    protected void write(ByteBuf buf, Integer val) {
        buf.writeInt(val);
    }
}
