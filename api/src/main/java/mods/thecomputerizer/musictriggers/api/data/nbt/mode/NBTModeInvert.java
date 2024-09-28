package mods.thecomputerizer.musictriggers.api.data.nbt.mode;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.BaseTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;

public class NBTModeInvert extends NBTMode {

    public NBTModeInvert(String name) {
        super(name);
    }

    @Override protected boolean checkMatchChild(ChannelAPI channel, CompoundTagAPI<?> tag, boolean parentResult) {
        return !parentResult;
    }

    @Override protected boolean checkMatchInner(ChannelAPI channel, CompoundTagAPI<?> tag) {
        BaseTagAPI<?> val = stepToTag(tag,this.split.length);
        return Objects.isNull(val) || (val.isPrimitive() && !val.asPrimitiveTag().asBoolean());
    }

    @Override public @Nonnull Collection<Supplier<NBTMode>> getPotentialChildren() {
        return Collections.emptyList();
    }
}
