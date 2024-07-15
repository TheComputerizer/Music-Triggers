package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectionLink extends DataLink {
    
    public static SelectionLink singleSingle(MTScreenInfo type, Consumer<ParameterWrapper> singleSelect) {
        return new SelectionLink(type,false,false,singleSelect,null);
    }
    
    public static SelectionLink singleMulti(MTScreenInfo type, Consumer<Collection<ParameterWrapper>> multiSelect) {
        return new SelectionLink(type,false,true,null,multiSelect);
    }
    
    public static SelectionLink dualSingle(MTScreenInfo type, Consumer<ParameterWrapper> singleSelect) {
        return new SelectionLink(type,true,false,singleSelect,null);
    }
    
    public static SelectionLink dualMulti(MTScreenInfo type, Consumer<Collection<ParameterWrapper>> multiSelect) {
        return new SelectionLink(type,true,true,null,multiSelect);
    }
    
    private final boolean multi;
    private final Consumer<ParameterWrapper> singleSelect;
    private final Consumer<Collection<ParameterWrapper>> multiSelect;
    private final Set<SelectionElement> selected;
    private final Map<Function<SelectionElement,String>,Function<DataList,SelectionElement>> elementMakers;
    private final Map<Function<SelectionElement,String>,Function<DataList,SelectionElement>> otherElementMakers;
    
    SelectionLink(MTScreenInfo type, boolean dual, boolean multi, Consumer<ParameterWrapper> singleSelect,
            Consumer<Collection<ParameterWrapper>> multiSelect) {
        super(type,dual);
        this.multi = multi;
        this.singleSelect = singleSelect;
        this.multiSelect = multiSelect;
        this.selected = new HashSet<>();
        this.elementMakers = new HashMap<>();
        this.otherElementMakers = new HashMap<>();
    }
    
    private DataList addElements(MTGUIScreen screen, double x, double width,
            Map<Function<SelectionElement,String>,Function<DataList,SelectionElement>> makers, boolean left) {
        DataList list = new DataList(x,0d,width,1.8d,0.05d);
        Map<SelectionElement,Function<SelectionElement,String>> unsorted = new HashMap<>();
        for(Entry<Function<SelectionElement,String>,Function<DataList,SelectionElement>> entry : makers.entrySet())
            unsorted.put(entry.getValue().apply(list),entry.getKey());
        List<SelectionElement> sorted = new ArrayList<>(unsorted.keySet());
        sorted.sort(Comparator.comparing(w -> unsorted.get(w).apply(w)));
        for(SelectionElement element : sorted) list.addWidget(element.widget);
        return list;
    }
    
    public void addOtherElementMaker(Function<SelectionElement,String> sorter, Function<DataList,SelectionElement> maker) {
        this.otherElementMakers.put(sorter, maker);
    }
    
    public void addElementMaker(Function<SelectionElement,String> sorter, Function<DataList,SelectionElement> maker) {
        this.elementMakers.put(sorter,maker);
    }
    
    void applySelected(MTGUIScreen screen) {
        Set<ParameterWrapper> wrappers = new HashSet<>();
        for(SelectionElement element : this.selected) {
            if(!this.multi) {
                if(Objects.nonNull(this.singleSelect)) this.singleSelect.accept(element.selectedSupplier.get());
                MTGUIScreen.open(screen);
                return;
            }
            wrappers.add(element.selectedSupplier.get());
        }
        if(this.multi && Objects.nonNull(this.multiSelect)) this.multiSelect.accept(wrappers);
        MTGUIScreen.open(screen);
    }
    
    @Override public TextAPI<?> getDescription() {
        return MTGUIScreen.selectionDesc(this.type.getType());
    }
    
    @Override public TextAPI<?> getDisplayName() {
        return MTGUIScreen.selectionName(this.type.getType());
    }
    
    public DataList getList(MTGUIScreen screen) {
        return addElements(screen, this.dual ? -0.5d : 0d, this.dual ? 1d : 2d,this.elementMakers,true);
    }
    
    public DataList getOtherList(MTGUIScreen screen) {
        return this.dual ? addElements(screen,0.5d,1d,this.otherElementMakers,false) : null;
    }
    
    @Override public void populateToml(Toml toml) {}
    
    public void select(SelectionElement element, MTGUIScreen screen) {
        if(this.multi) {
            this.selected.add(element);
            element.selected = true;
        }
        else {
            for(SelectionElement e : this.selected) e.selected = false;
            this.selected.clear();
            this.selected.add(element);
            element.selected = true;
            applySelected(screen);
        }
    }
    
    public void unselect(SelectionElement element) {
        this.selected.remove(element);
        element.selected = false;
    }
    
    public static final class SelectionElement {
        
        final SelectionLink parent;
        final Supplier<ParameterWrapper> selectedSupplier;
        final Widget widget;
        @Getter boolean selected;
        
        public SelectionElement(SelectionLink parent, Function<SelectionElement,Widget> widgetMaker,
                Supplier<ParameterWrapper> selectedSupplier) {
            this.parent = parent;
            this.selectedSupplier = selectedSupplier;
            this.widget = widgetMaker.apply(this);
        }
        
        public void onLeftClick(MTGUIScreen screen) {
            if(!this.selected) this.parent.select(this,screen);
            else this.parent.unselect(this);
        }
    }
}