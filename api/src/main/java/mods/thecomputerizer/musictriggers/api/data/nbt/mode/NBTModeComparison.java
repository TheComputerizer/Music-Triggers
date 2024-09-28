package mods.thecomputerizer.musictriggers.api.data.nbt.mode;

import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.ListTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.PrimitiveTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.StringTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Patterns;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public abstract class NBTModeComparison extends NBTMode {

    protected String comparison;

    protected NBTModeComparison(String name) {
        super(name);
    }

    protected int compareCompound(CompoundTagAPI<?> tag) {
        return Integer.MIN_VALUE;
    }

    protected int compareList(ListTagAPI<?> tag) {
        return Integer.MIN_VALUE;
    }

    protected int comparePrimitive(PrimitiveTagAPI<?> tag) {
        Object primitive = parseAsPrimitive();
        switch(primitive.getClass().getSimpleName()) {
            case "Boolean": return Boolean.compare(tag.asBoolean(),(boolean)primitive);
            case "Byte": return Byte.compare(tag.asByte(),(byte)primitive);
            case "Double": return Double.compare(tag.asDouble(),(double)primitive);
            case "Float": return Float.compare(tag.asFloat(),(float)primitive);
            case "Integer": return Integer.compare(tag.asInt(),(int)primitive);
            case "Long": return Long.compare(tag.asLong(),(long)primitive);
            case "Short": return Float.compare(tag.asShort(),(short)primitive);
            default: return Double.compare(tag.asDouble(),Double.parseDouble(this.comparison));
        }
    }

    protected int compareString(StringTagAPI<?> tag) {
        return tag.getValue().compareTo(this.comparison);
    }

    protected Object parseAsPrimitive() {
        if(Patterns.matchesAny(this.comparison,false,"true","false"))
            return Boolean.parseBoolean(this.comparison);
        if(StringUtils.endsWithAny(this.comparison,"b","B"))
            return Byte.parseByte(this.comparison.substring(0,this.comparison.length()-1));
        if(StringUtils.endsWithAny(this.comparison,"d","D"))
            return Double.parseDouble(this.comparison.substring(0,this.comparison.length()-1));
        if(StringUtils.endsWithAny(this.comparison,"f","F"))
            return Float.parseFloat(this.comparison.substring(0,this.comparison.length()-1));
        if(StringUtils.endsWithAny(this.comparison,"l","L"))
            return Long.parseLong(this.comparison.substring(0,this.comparison.length()-1));
        if(StringUtils.endsWithAny(this.comparison,"s","S"))
            return Short.parseShort(this.comparison.substring(0,this.comparison.length()-1));
        if(this.comparison.contains(".")) return Double.parseDouble(this.comparison);
        return Integer.parseInt(this.comparison);
    }

    @Override public void setSplit(String[] split) {
        this.comparison = hasValidSplit() ? split[split.length-1] : null;
        this.split = Arrays.copyOfRange(split,0,split.length-1);
    }
}