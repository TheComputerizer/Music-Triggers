package mods.thecomputerizer.musictriggers.api.data.parameter;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.*;
import mods.thecomputerizer.theimpossiblelibrary.api.core.ReflectionHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

public class ParameterHelper {

    /**
     * Does not support collections
     */
    @SuppressWarnings("unchecked")
    public static <E> Parameter<E> parameterize(Class<E> type, E element) {
        if(Collection.class.isAssignableFrom(type))
            return (Parameter<E>)new ParameterList<>(String.class,(List<String>)element); //TODO Should this really be restricted to lists of strings?
        switch(type.getSimpleName()) {
            case "Boolean": return (Parameter<E>)new ParameterBool((Boolean)element);
            case "Byte": return (Parameter<E>)new ParameterByte((Byte)element);
            case "Double": return (Parameter<E>)new ParameterDouble((Double)element);
            case "Float": return (Parameter<E>)new ParameterFloat((Float)element);
            case "Integer": return (Parameter<E>)new ParameterInt((Integer)element);
            case "Long": return (Parameter<E>)new ParameterLong((Long)element);
            case "Short": return (Parameter<E>)new ParameterShort((Short)element);
            default: return (Parameter<E>)new ParameterString((String)element);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static @Nullable Parameter<?> parse(ByteBuf buf) {
        Class<?> clazz = ReflectionHelper.findExtensibleClass(NetworkHelper.readString(buf),Parameter.class);
        Constructor<?> constructor = ReflectionHelper.findConstructor(clazz,ByteBuf.class);
        try {
            return (Parameter<?>)constructor.newInstance(buf);
        } catch(ReflectiveOperationException ex) {
            MTRef.logError("Unable to invoke constructor `{}` for paremter!",constructor);
            return null;
        }
    }
}