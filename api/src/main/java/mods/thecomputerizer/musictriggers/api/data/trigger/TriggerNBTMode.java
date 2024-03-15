package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

public enum TriggerNBTMode {

    EQUAL("EQUAL",new String[]{"INVERT"},(tag,splits) -> false),
    GREATER("GREATER",new String[]{"EQUAL"},(tag,splits) -> false),
    INVERT("INVERT",new String[]{},(tag,splits) -> false),
    KEY_PRESENT("KEY_PRESENT",new String[]{"INVERT"},(tag,splits) -> false),
    LESSER("LESSER",new String[]{"EQUAL"},(tag,splits) -> false),
    VAL_PRESENT("VAL_PRESENT",new String[]{"INVERT"},(tag,splits) -> false);

    private static final Map<String,TriggerNBTMode> BY_NAME = new HashMap<>();

    public static @Nullable TriggerNBTMode get(String name) {
        return BY_NAME.get(name);
    }

    public static @Nullable List<TriggerNBTMode> getQualified(String[] splitTags) {
        if(splitTags.length>1) {
            List<TriggerNBTMode> modes = new ArrayList<>();
            TriggerNBTMode mode;
            for(int i=0;i<splitTags.length-1;i++) {
                mode = get(splitTags[i]);
                if(Objects.nonNull(mode)) modes.add(mode);
                if(Objects.isNull(mode) || !mode.acceptsNext(splitTags[1+1])) break;
            }
            return modes;
        }
        return Collections.emptyList();
    }

    @Getter private final String name;
    private final String[] acceptedAfter;
    private final BiFunction<CompoundTagAPI,String[],Boolean> validator;

    TriggerNBTMode(String name, String[] acceptedAfter, BiFunction<CompoundTagAPI,String[],Boolean> validator) {
        this.name = name;
        this.acceptedAfter = acceptedAfter;
        this.validator = validator;
    }

    public boolean acceptsNext(@Nullable TriggerNBTMode mode) {
        return Objects.nonNull(mode) && acceptsNext(mode.name);
    }

    public boolean acceptsNext(String mode) {
        for(String next : this.acceptedAfter)
            if(next.equals(mode)) return true;
        return false;
    }


    static {
        for(TriggerNBTMode mode : values()) BY_NAME.put(mode.name,mode);
    }
}