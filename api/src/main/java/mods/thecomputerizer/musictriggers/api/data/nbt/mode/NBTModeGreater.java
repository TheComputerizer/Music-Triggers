package mods.thecomputerizer.musictriggers.api.data.nbt.mode;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.nbt.NBTHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.BaseTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

public class NBTModeGreater extends NBTModeComparison {

    public NBTModeGreater(String name) {
        super(name);
    }

    @Override protected boolean checkMatchChild(ChannelAPI channel, CompoundTagAPI<?> tag, boolean parentResult) {
        return parentResult && checkMatchInner(channel,tag);
    }

    @Override protected boolean checkMatchInner(ChannelAPI channel, CompoundTagAPI<?> tag) {
        BaseTagAPI<?> val = stepToTag(tag,this.split.length);
        if(Objects.isNull(val)) return false;
        if(val.isPrimitive()) return comparePrimitive(val.asPrimitiveTag())>0;
        if(val.isString()) return compareString(val.asStringTag())>0;
        return false;
    }

    @Override public @Nonnull Collection<Supplier<NBTMode>> getPotentialChildren() {
        return Arrays.asList(() -> NBTHelper.getMode("EQUAL"),() -> NBTHelper.getMode("INVERT"));
    }
}