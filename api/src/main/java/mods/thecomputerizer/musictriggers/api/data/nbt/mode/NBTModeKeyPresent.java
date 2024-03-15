package mods.thecomputerizer.musictriggers.api.data.nbt.mode;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.nbt.NBTHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.BaseTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;

public class NBTModeKeyPresent extends NBTModeComparison {

    public NBTModeKeyPresent(String name) {
        super(name);
    }

    @Override
    protected boolean checkMatchChild(ChannelAPI channel, CompoundTagAPI tag, boolean parentResult) {
        return parentResult && checkMatchInner(channel,tag);
    }

    @Override
    protected boolean checkMatchInner(ChannelAPI channel, CompoundTagAPI tag) {
        BaseTagAPI val = stepToTag(tag,this.split.length);
        return Objects.nonNull(val) && val.isCompound();
    }

    @Override
    public @Nonnull Collection<Supplier<NBTMode>> getPotentialChildren() {
        return Collections.singletonList(() -> NBTHelper.getMode("INVERT"));
    }
}