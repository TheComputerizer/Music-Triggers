package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

public class ParameterByte extends ParameterNumber<Byte> {

    public ParameterByte(byte defaultValue) {
        super(defaultValue);
    }

    public ParameterByte(ByteBuf buf) {
        super(buf);
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
