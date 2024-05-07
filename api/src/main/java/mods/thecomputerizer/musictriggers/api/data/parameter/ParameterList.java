package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ClassHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.GenericUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParameterList<E> extends Parameter<List<E>> { //TODO Does not currently support nested lists

    protected final Class<E> type;

    public ParameterList(Class<E> type, List<E> defaults) {
        super(defaults);
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public ParameterList(ByteBuf buf) {
        super(buf);
        this.type = (Class<E>)ClassHelper.findClass(NetworkHelper.readString(buf));
    }
    
    @SuppressWarnings("unchecked") @Override
    protected List<E> read(ByteBuf buf) {
        return (List<E>)NetworkHelper.readList(buf,() -> GenericUtils.parseGenericType(NetworkHelper.readString(buf),this.type));
    }
    
    @SuppressWarnings("unchecked") @Override
    public void setValue(@Nullable Object value) {
        if(Objects.isNull(value)) return;
        List<E> list = new ArrayList<>();
        if(value instanceof List<?>) list.addAll((List<E>)value);
        else list.add((E)value);
        this.value = list;
    }

    @Override
    protected void write(ByteBuf buf, List<E> val) {
        NetworkHelper.writeString(buf,this.type.getName());
        NetworkHelper.writeList(buf,val,e -> NetworkHelper.writeString(buf,e.toString()));
    }
}
