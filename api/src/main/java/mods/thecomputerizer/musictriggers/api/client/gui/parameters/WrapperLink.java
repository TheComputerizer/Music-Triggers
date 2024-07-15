package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen.constructScreen;
import static mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen.open;

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
    
    private DataList addElements(MTGUIScreen screen, double x, double width, Collection<WrapperElement> wrappers) {
        DataList list = new DataList(x,0d,width,1.8d,0.05d);
        List<WrapperElement> ordered = new ArrayList<>(wrappers);
        ordered.sort(Comparator.comparing(e -> e.getDisplayName().toString()));
        for(WrapperElement element : ordered) {
            list.addButton(element.getDisplayName(),b ->
                    open(constructScreen(screen,element.link.type,ClientHelper.getWindow(),ClientHelper.getGuiScale())));
        }
        return list;
    }
    
    public DataList getList(MTGUIScreen screen) {
        return addElements(screen,this.dual ? -0.5d : 0d,this.dual ? 1d : 2d,this.wrappers);
    }
    
    public @Nullable DataList getOtherList(MTGUIScreen screen) {
        return this.dual ? addElements(screen,0.5d,1d,this.otherWrappers) : null;
    }
    
    @Override public boolean isModified() {
        if(this.modified) return true;
        for(WrapperElement wrapper : this.wrappers)
            if(wrapper.link.isModified()) return true;
        for(WrapperElement wrapper : this.otherWrappers)
            if(wrapper.link.isModified()) return true;
        return false;
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
    
    @Override public void populateToml(Toml toml) {
        Toml parent = toml;
        String typeName = this.type.getType();
        if(Misc.equalsAny(typeName,"main","renders")) parent = Toml.getEmpty();
        for(WrapperElement wrapper : this.wrappers) {
            Toml next = Toml.getEmpty();
            wrapper.link.populateToml(next);
            if(!next.getEntryValuesAsMap().isEmpty() || !next.getAllTables().isEmpty())
                parent.addTable(wrapper.link.type.getType(),next);
        }
        if(typeName.equals("main")) {
            if(!parent.getAllTables().isEmpty()) toml.addTable("songs",parent);
            parent = Toml.getEmpty();
        }
        else if(typeName.equals("renders")) {
            if(!parent.getAllTables().isEmpty()) toml.addTable("image",parent);
            parent = Toml.getEmpty();
        }
        for(WrapperElement wrapper : this.otherWrappers) {
            Toml next = Toml.getEmpty();
            wrapper.link.populateToml(next);
            if(!next.getEntryValuesAsMap().isEmpty() || !next.getAllTables().isEmpty())
                parent.addTable(wrapper.link.type.getType(),next);
        }
        if(typeName.equals("main") && !parent.getAllTables().isEmpty()) toml.addTable("triggers",parent);
        else if(typeName.equals("renders") && !parent.getAllTables().isEmpty()) toml.addTable("title",parent);
    }
    
    public static final class WrapperElement {
        
        final WrapperLink parent;
        final DataLink link;
        
        WrapperElement(WrapperLink parent, ParameterWrapper wrapper) {
            this.parent = parent;
            String nextType = wrapper instanceof RedirectElement ? "redirect_element" :
                    (wrapper instanceof RecordElement ? "jukebox_element" : wrapper.getName());
            MTScreenInfo next = parent.type.next(nextType);
            this.link = wrapper.getLink(next);
            next.setLink(this.link);
        }
        
        TextAPI<?> getDisplayName() {
            return this.parent.wrapperName(((ParameterLink)this.link).getWrapper().getName());
        }
        
        TextAPI<?> getDescription() {
            return this.parent.wrapperDesc(this.link.type.getType());
        }
    }
}
