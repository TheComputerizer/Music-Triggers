package mods.thecomputerizer.musictriggers.api.data.nbt.mode;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.BaseTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public abstract class NBTMode {

    @Getter private final String name;
    @Setter protected String[] split;
    private Collection<NBTMode> potentialChildren;
    protected NBTMode child;

    protected NBTMode(String name) {
        this.name = name;
    }

    protected void findChild(NBTMode parent) {
        if(hasValidSplit()) {
            for(NBTMode mode : this.potentialChildren) {
                if(mode.name.equals(this.split[0])) {
                    setChild(mode);
                    break;
                }
            }
        }
        if(Objects.nonNull(this.child)) {
            findChild(this.child);
            this.split = this.child.split;
        }
    }

    public boolean checkMatch(ChannelAPI channel, BaseTagAPI<?> tag) {
        this.potentialChildren = initPotentialChildren();
        findChild(this);
        boolean ret = false;
        try {
            ret = hasValidSplit() && tag.isCompound() && checkMatchInner(channel,tag.asCompoundTag());
        } catch(NumberFormatException ex) {
            channel.logError("Tried to check numerical value of NBT data against a non numerical value in `{}`",
                    this.split,ex);
        } catch(Exception ex) {
            channel.logError("Caught an unknown error when attempting to check NBT data for `{}`",this.split,ex);
        }
        this.split = null;
        this.child = null;
        return ret;
    }

    protected abstract boolean checkMatchChild(ChannelAPI channel, CompoundTagAPI<?> tag, boolean parentResult);

    protected abstract boolean checkMatchInner(ChannelAPI channel, CompoundTagAPI<?> tag);

    protected @Nullable CompoundTagAPI<?> getNextCompound(@Nullable CompoundTagAPI<?> tag) {
        BaseTagAPI<?> based = getNextTag(tag);
        return Objects.nonNull(based) && based.isCompound() ? based.asCompoundTag() : null;
    }

    protected @Nullable BaseTagAPI<?> getNextTag(@Nullable CompoundTagAPI<?> tag) {
        String name = stepSplit();
        return Objects.nonNull(name) && Objects.nonNull(tag) ? tag.getTag(name) : null;
    }

    public abstract @Nonnull Collection<Supplier<NBTMode>> getPotentialChildren();

    public boolean hasValidSplit() {
        return Objects.nonNull(this.split) && this.split.length>0;
    }

    protected Collection<NBTMode> initPotentialChildren() {
        if(Objects.nonNull(this.potentialChildren)) return this.potentialChildren;
        Set<NBTMode> set = new HashSet<>();
        for(Supplier<NBTMode> childSupplier : getPotentialChildren()) {
            NBTMode child = childSupplier.get();
            if(Objects.nonNull(child)) set.add(child);
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Assumes hasValidSplit has already been called
     */
    protected void setChild(NBTMode child) {
        child.split = Arrays.copyOfRange(this.split,1,this.split.length);
        this.child = child;
    }

    protected @Nullable String stepSplit() {
        String next = hasValidSplit() ? this.split[0] : null;
        if(Objects.nonNull(next)) setSplit(Arrays.copyOfRange(this.split,1,this.split.length));
        return next;
    }

    protected @Nullable BaseTagAPI<?> stepToTag(CompoundTagAPI<?> tag, int steps) {
        while(Objects.nonNull(tag) && steps>1) tag = getNextCompound(tag);
        return getNextTag(tag);
    }
}
