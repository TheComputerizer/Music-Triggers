package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

public class ParameterFloat extends ParameterNumber<Float> {

    public ParameterFloat(float defaultValue) {
        super(defaultValue);
    }

    public ParameterFloat(ByteBuf buf) {
        super(buf);
    }

    @Override
    public void appendToTable(Holder holder, Table table, String name) {
        holder.addVariable(table,name,getValue());
    }

    @Override
    protected Float read(ByteBuf buf) {
        return buf.readFloat();
    }

    @Override
    protected void parseValueInner(String unparsed) {
        this.value = Float.parseFloat(unparsed);
    }

    @Override
    protected void write(ByteBuf buf, Float val) {
        buf.writeFloat(val);
    }
}
