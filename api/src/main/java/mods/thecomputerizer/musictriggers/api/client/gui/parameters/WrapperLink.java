package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;

public class WrapperLink extends DataLink {
    
    private final Set<WrapperElement> wrappers;
    private final Set<WrapperElement> otherWrappers;
    
    public WrapperLink(MTScreenInfo type, Collection<? extends ParameterWrapper> wrappers) {
        this(type,false,wrappers,Collections.emptySet());
    }
    
    public WrapperLink(MTScreenInfo type, Collection<? extends ParameterWrapper> wrappers,
            Collection<? extends ParameterWrapper> otherWrappers) {
        this(type,true,wrappers,otherWrappers);
    }
    
    private WrapperLink(MTScreenInfo type, boolean dual, Collection<? extends ParameterWrapper> wrappers,
            Collection<? extends ParameterWrapper> otherWrappers) {
        super(type,dual);
        this.wrappers = new HashSet<>();
        for(ParameterWrapper wrapper : wrappers) this.wrappers.add(new WrapperElement(this,wrapper));
        this.otherWrappers = new HashSet<>();
        for(ParameterWrapper wrapper : otherWrappers) this.otherWrappers.add(new WrapperElement(this,wrapper));
    }
    
    public DataList getList() {
        DataList list = new DataList(this.dual ? -0.5d : 0d,0d,this.dual ? 1d : 2d,1.8d,0.05d);
        for(WrapperElement element : this.wrappers) {
            list.addButton(element.getDisplayName(),b -> {});
        }
        return list;
    }
    
    public @Nullable DataList getOtherList() {
        if(!this.dual) return null;
        DataList list = new DataList(0.5d,0d,1d,1.8d,0.05d);
        for(WrapperElement element : this.otherWrappers) {
            list.addButton(element.getDisplayName(),b -> {});
        }
        return list;
    }
    
    private TextAPI<?> wrapperDesc(String name) {
        return TextHelper.getTranslated(wrapperLang(name,"desc"));
    }
    
    private String wrapperLang(String name, String suffix) {
        return String.format("selection.%1$s.%2$s.%3$s",MODID,name,suffix);
    }
    
    private TextAPI<?> wrapperName(String name) {
        return Misc.equalsAny(this.type.getType(),"jukebox","main","redirect") ? TextHelper.getLiteral(name) :
                TextHelper.getTranslated(wrapperLang(name,"name"));
    }
    
    public static final class WrapperElement {
        
        final WrapperLink parent;
        final ParameterWrapper wrapper;
        
        WrapperElement(WrapperLink parent, ParameterWrapper wrapper) {
            this.parent = parent;
            this.wrapper = wrapper;
        }
        
        TextAPI<?> getDisplayName() {
            return this.parent.wrapperName(this.wrapper.getName());
        }
        
        TextAPI<?> getDescription() {
            return this.parent.wrapperDesc(this.wrapper.getName());
        }
    }
}
