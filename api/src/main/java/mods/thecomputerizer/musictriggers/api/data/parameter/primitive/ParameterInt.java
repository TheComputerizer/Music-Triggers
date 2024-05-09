package mods.thecomputerizer.musictriggers.api.data.parameter.primitive;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;

public class ParameterInt extends ParameterNumber<Integer> {

    public ParameterInt(int defaultValue) {
        super(defaultValue);
    }
    
    @SuppressWarnings("unused") //See ParameterHelper#parse
    public ParameterInt(ByteBuf buf) {
        super(buf);
    }

    @Override
    protected Integer read(ByteBuf buf) {
        return buf.readInt();
    }
    
    @Override public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) this.value = 0;
        else if(value instanceof Boolean) this.value = (Boolean)value ? 1 : 0;
        else if(value instanceof Number) this.value = ((Number)value).intValue();
        else this.value = Integer.parseInt(value.toString());
    }
    
    @Override
    protected void write(ByteBuf buf, Integer val) {
        buf.writeInt(val);
    }
}
