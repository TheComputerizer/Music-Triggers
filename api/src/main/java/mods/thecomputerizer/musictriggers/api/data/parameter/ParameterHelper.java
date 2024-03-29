package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.*;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ParameterHelper {

    public static <E> List<Parameter<E>> parameterize(Class<E> type, List<E> list) {
        List<Parameter<E>> parameters = new ArrayList<>();
        for(E element : list) parameters.add(parameterize(type,element));
        return parameters;
    }

    /**
     * Does not support collections
     */
    @SuppressWarnings("unchecked")
    public static <E> Parameter<E> parameterize(Class<E> type, E element) {
        switch(type.getSimpleName()) {
            case "Boolean": return (Parameter<E>)new ParameterBoolean((boolean)element);
            case "Byte": return (Parameter<E>)new ParameterByte((byte)element);
            case "Double": return (Parameter<E>)new ParameterDouble((byte)element);
            case "Float": return (Parameter<E>)new ParameterFloat((byte)element);
            case "Integer": return (Parameter<E>)new ParameterInt((byte)element);
            case "Long": return (Parameter<E>)new ParameterLong((byte)element);
            case "Short": return (Parameter<E>)new ParameterShort((byte)element);
            default: return (Parameter<E>)new ParameterString((String)element);
        }
    }

    public static @Nullable Parameter<?> parse(ByteBuf buf) {
        Class<?> clazz = Misc.findExtensibleClass(NetworkHelper.readString(buf),Parameter.class);
        Constructor<?> constructor = Misc.findConstructor(clazz,ByteBuf.class);
        try {
            return (Parameter<?>)constructor.newInstance(buf);
        } catch(ReflectiveOperationException ex) {
            MTRef.logError("Unable to invoke constructor `{}` for paremter!",constructor);
            return null;
        }
    }
}