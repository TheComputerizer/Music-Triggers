package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.GenericUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParameterList<E> extends Parameter<List<Parameter<E>>> {

    protected final Class<E> type;
    protected final List<E> values;

    public ParameterList(Class<E> type, List<E> defaults) {
        super(ParameterHelper.parameterize(type,defaults));
        this.type = type;
        this.values = defaults;
    }

    public ParameterList(ByteBuf buf) {
        super(buf);
        this.type = findClass(NetworkHelper.readString(buf));
        this.values = new ArrayList<>();
        for(Parameter<E> parameter : getValue()) this.values.add(parameter.value);
    }

    @SuppressWarnings("unchecked")
    protected @Nullable Class<E> findClass(String name) {
        Class<?> clazz = ParameterHelper.findClass(name);
        return Objects.nonNull(clazz) ? (Class<E>)clazz : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Parameter<E>> read(ByteBuf buf) {
        List<Parameter<E>> parameters = new ArrayList<>();
        int size = buf.readInt();
        while(size>0) {
            Parameter<?> parsed = ParameterHelper.parse(buf);
            parameters.add((Parameter<E>)parsed);
            this.values.add((E)parsed.getValue());
            size--;
        }
        return parameters;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void parseValueInner(String unparsed) {
        this.value = new ArrayList<>();
        String[] elements = unparsed.split(",");
        for(String element : elements) {
            E e = (E)GenericUtils.parseGenericType(element,this.type);
            this.value.add(ParameterHelper.parameterize(this.type,e));
            this.values.add(e);
        }
    }

    @Override
    protected void write(ByteBuf buf, List<Parameter<E>> val) {
        NetworkHelper.writeString(buf,this.type.getName());
        NetworkHelper.writeGenericList(buf,val,(buf1,p) -> p.write(buf1));
    }
}
