package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

public class ParameterLong extends ParameterNumber<Long> {

    public ParameterLong(long defaultValue) {
        super(defaultValue);
    }

    public ParameterLong(ByteBuf buf) {
        super(buf);
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
