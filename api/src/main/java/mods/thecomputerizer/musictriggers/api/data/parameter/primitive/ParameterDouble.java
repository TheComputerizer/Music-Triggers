package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

public class ParameterDouble extends ParameterNumber<Double> {

    public ParameterDouble(double defaultValue) {
        super(defaultValue);
    }

    public ParameterDouble(ByteBuf buf) {
        super(buf);
    }

    @Override
    protected Double read(ByteBuf buf) {
        return buf.readDouble();
    }

    @Override
    protected void parseValueInner(String unparsed) {
        this.value = Double.parseDouble(unparsed);
    }

    @Override
    protected void write(ByteBuf buf, Double val) {
        buf.writeDouble(val);
    }
}
