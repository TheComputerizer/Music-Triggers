package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;

@SuppressWarnings("unused")
public abstract class ParameterNumber<N extends Number> extends Parameter<N> {

    protected ParameterNumber(N defaultValue) {
        super(defaultValue);
    }

    protected ParameterNumber(ByteBuf buf) {
        super(buf);
    }

    public byte byteValue() {
        return this.value.byteValue();
    }
    
    @Override public abstract ParameterNumber<N> copy();

    public byte defaultByteValue() {
        return this.defaultValue.byteValue();
    }

    public double defaultDoubleValue() {
        return this.defaultValue.doubleValue();
    }

    public float defaultFloatValue() {
        return this.defaultValue.floatValue();
    }

    public int defaultIntValue() {
        return this.defaultValue.intValue();
    }

    public long defaultLongValue() {
        return this.defaultValue.longValue();
    }

    public short defaultShortValue() {
        return this.defaultValue.shortValue();
    }

    public double doubleValue() {
        return this.value.doubleValue();
    }

    public float floatValue() {
        return this.value.floatValue();
    }

    public int intValue() {
        return this.value.intValue();
    }
    
    @Override public boolean isBool() {
        return false;
    }
    
    @Override public boolean isList() {
        return false;
    }
    
    @Override public boolean isNumber() {
        return true;
    }
    
    @Override public boolean isPrimitive() {
        return true;
    }
    
    @Override public boolean isString() {
        return false;
    }

    public long longValue() {
        return this.value.longValue();
    }

    public short shortValue() {
        return this.value.shortValue();
    }
}