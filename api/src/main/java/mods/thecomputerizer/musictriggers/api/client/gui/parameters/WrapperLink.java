package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef.Loop;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.command.CommandElement;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle.From;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle.To;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.render.TitleElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.Link;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.HolderTrigger;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
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
import java.util.Objects;
import java.util.Set;

import static mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen.constructScreen;
import static mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen.open;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.AQUA;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.DARK_RED;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.GRAY;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.RED;

@Getter
public class WrapperLink extends DataLink {
    
    private final Set<WrapperElement> wrappers;
    private final Set<WrapperElement> otherWrappers;
    
    public WrapperLink(Collection<? extends ParameterWrapper> wrappers) {
        this(false,wrappers,Collections.emptySet());
    }
    
    public WrapperLink(Collection<? extends ParameterWrapper> wrappers,
            Collection<? extends ParameterWrapper> otherWrappers) {
        this(true,wrappers,otherWrappers);
    }
    
    private WrapperLink(boolean dual, Collection<? extends ParameterWrapper> wrappers,
            Collection<? extends ParameterWrapper> otherWrappers) {
        super(dual);
        this.wrappers = new HashSet<>();
        for(ParameterWrapper wrapper : wrappers) this.wrappers.add(new WrapperElement(this,wrapper));
        this.otherWrappers = new HashSet<>();
        for(ParameterWrapper wrapper : otherWrappers) this.otherWrappers.add(new WrapperElement(this,wrapper));
    }
    
    private DataList addElements(MTGUIScreen screen, double x, double width, Collection<WrapperElement> wrappers,
            boolean left) {
        DataList list = new DataList(x,0d,width,1.8d,0.05d);
        List<WrapperElement> ordered = new ArrayList<>(wrappers);
        ordered.sort(Comparator.comparing(e -> e.getDisplayName().toString()));
        for(WrapperElement element : ordered) {
            Button button = list.makeButton(element.getDisplayName(),b -> {
                if(!deleteElement(list,b,element,wrappers))
                    open(constructScreen(screen,element.link.type,ClientHelper.getWindow(),ClientHelper.getGuiScale()));
            });
            button.addHoverLine(element.getDescription());
            element.widget = button;
            list.addWidget(button);
        }
        SpecialButton add = list.makeSpecialButton(this.type.getSpecialLang("gui","button.add_entry.name"),b -> {
            Collection<Widget> widgets = list.getWidgets();
            for(Widget w : widgets)
                if(w instanceof Button && ((Button)w).getText().getColor()==RED) return;
            widgets.remove(b);
            addNewWrapper(screen,list,wrappers,left);
            widgets.add(b);
            list.setWidgets(widgets);
            this.setModified(true);
        });
        add.getText().setColor(GRAY.withAlpha(0.7f));
        add.setContextFunc(b -> {
            SpecialButton special = (SpecialButton)b;
            boolean delete = special.isDeleting();
            b.getText().setColor(delete ? GRAY.withAlpha(0.7f) : RED);
            TextAPI<?> text = this.type.getSpecialLang("gui",
                    "button."+(delete ? "add" : "remove")+"_entry.name");
            b.setText(text);
            b.setHoverText(h -> {
                h.setColor(delete ? AQUA : DARK_RED);
                h.setText(text);
            });
            special.setDeleting(!delete);
            ScreenHelper.playVanillaClickSound();
        });
        list.addWidget(add);
        return list;
    }
    
    public void addNewWrapper(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers, ParameterWrapper wrapper) {
        WrapperElement element = getElementFor(wrapper);
        wrappers.add(element);
        Collection<Widget> widgets = list.getWidgets();
        Widget widget = null;
        for(Widget w : widgets) {
            if(w instanceof SpecialButton) {
                widget = w;
                break;
            }
        }
        if(Objects.nonNull(widget)) widgets.remove(widget);
        Button button = list.makeButton(element.getDisplayName(),b -> {
            if(!deleteElement(list,b,element,wrappers))
                open(constructScreen(screen,element.link.type,ClientHelper.getWindow(),ClientHelper.getGuiScale()));
        });
        button.addHoverLine(element.getDescription());
        element.widget = button;
        list.addWidget(button);
        if(list.getWidgets() instanceof List<?>)
            ((List<Widget>)list.getWidgets()).sort(Comparator.comparing(w -> ((Button)w).getText().toString()));
        if(Objects.nonNull(widget)) list.addWidget(widget);
    }
    
    public void addNewWrapper(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers, boolean left) {
        ParameterWrapper wrapper = null;
        ChannelAPI channel = this.type.getChannel();
        switch(this.type.getType()) {
            case "commands": {
                wrapper = CommandElement.addToGui(screen.getTypeInfo());
                break;
            }
            case "from_list": {
                wrapper = From.addToGui(screen.getTypeInfo().getParent());
                break;
            }
            case "jukebox": {
                wrapper = new RecordElement(channel,"key = value");
                break;
            }
            case "links": {
                wrapper = Link.addToGui(screen.getTypeInfo().getParent());
                break;
            }
            case "loops": {
                wrapper = Loop.addToGui(screen.getTypeInfo().getParent());
                break;
            }
            case "main": {
                if(left) this.type.openAudioSelectionScreen(screen,list,wrappers);
                else this.type.openTriggerSelectionScreen(screen,list,wrappers);
                break;
            }
            case "redirect": {
                wrapper = new RedirectElement(channel,"key = value");
                break;
            }
            case "renders": {
                Toml toml = Toml.getEmpty();
                if(left) this.type.openImageSelectionScreen(screen,list,wrappers);
                else {
                    toml.addEntry("titles",new ArrayList<>());
                    wrapper = new TitleElement(channel);
                    wrapper.parse(toml);
                }
                break;
            }
            case "to_list": {
                wrapper = To.addToGui(screen.getTypeInfo().getParent());
                break;
            }
            case "toggles": {
                wrapper = new Toggle(channel.getHelper(),Toml.getEmpty());
                break;
            }
        }
        if(Objects.nonNull(wrapper)) addNewWrapper(screen,list,wrappers,wrapper);
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean deleteElement(DataList list, Button b, WrapperElement element, Collection<WrapperElement> wrappers) {
        Collection<Widget> widgets = list.getWidgets();
        boolean remove = false;
        for(Widget widget : widgets) {
            if(widget instanceof SpecialButton && ((SpecialButton)widget).isDeleting()) {
                remove = true;
                break;
            }
        }
        if(remove) {
            widgets.remove(b);
            wrappers.remove(element);
            list.setWidgets(widgets);
            setModified(true);
            return true;
        }
        return false;
    }
    
    @Override public TextAPI<?> getDescription() {
        return MTGUIScreen.selectionDesc(this.type.getType());
    }
    
    @Override public TextAPI<?> getDisplayName() {
        return MTGUIScreen.selectionName(this.type.getType());
    }
    
    public WrapperElement getElementFor(ParameterWrapper wrapper) {
        WrapperElement element = new WrapperElement(this,wrapper);
        if(Objects.nonNull(this.type)) element.setLink(this.type);
        return element;
    }
    
    public DataList getList(MTGUIScreen screen) {
        return addElements(screen,this.dual ? -0.5d : 0d,this.dual ? 1d : 2d,this.wrappers,true);
    }
    
    public @Nullable DataList getOtherList(MTGUIScreen screen) {
        return this.dual ? addElements(screen,0.5d,1d,this.otherWrappers,false) : null;
    }
    
    @Override public boolean isModified() {
        if(this.modified) return true;
        for(WrapperElement wrapper : this.wrappers)
            if(wrapper.link.isModified()) return true;
        for(WrapperElement wrapper : this.otherWrappers)
            if(wrapper.link.isModified()) return true;
        return false;
    }
    
    public void setElementDisplayName(ParameterLink link, TextAPI<?> name) {
        for(WrapperElement element : this.wrappers) {
            if(element.link==link) {
                element.setDisplayName(name);
                return;
            }
        }
        for(WrapperElement element : this.otherWrappers) {
            if(element.link==link) {
                element.setDisplayName(name);
                return;
            }
        }
    }
    
    @Override public void setType(MTScreenInfo type) {
        this.type = type;
        for(WrapperElement wrapper : this.wrappers) wrapper.setLink(type);
        for(WrapperElement wrapper : this.otherWrappers) wrapper.setLink(type);
    }
    
    private TextAPI<?> wrapperDesc(ParameterWrapper wrapper) {
        String name = wrapper.getName();
        if(wrapper instanceof AudioRef) {
            AudioRef ref = (AudioRef)wrapper;
            String type = (ref.isFile() ? MTGUIScreen.type("file") : MTGUIScreen.type("redirect"))+
                          " <"+ref.getLocation()+">";
            return MTGUIScreen.selectionDesc("audio",type,MTGUIScreen.triggerNames(ref.getTriggers()));
        }
        if(wrapper instanceof CommandElement)
            return MTGUIScreen.selectionDesc(name,wrapper.getParameterAsString("literal"));
        else if(wrapper instanceof Toggle)
            return MTGUIScreen.selectionDesc(name,((Toggle)wrapper).getTargetCount());
        else if(wrapper instanceof From)
            return MTGUIScreen.selectionDesc("from",wrapper.getParameterAsString("triggers"));
        else if(wrapper instanceof To)
            return MTGUIScreen.selectionDesc("to",wrapper.getParameterAsString("triggers"));
        else if(wrapper instanceof RedirectElement) name = "redirect";
        else if(wrapper instanceof RecordElement) name = "jukebox";
        else if(wrapper instanceof Link)
            return MTGUIScreen.selectionDesc("link",wrapper.getParameterAsList("linked_triggers"));
        else if(wrapper instanceof Loop)
            return MTGUIScreen.selectionDesc("loop",wrapper.getName());
        return wrapper instanceof TriggerAPI ? MTGUIScreen.triggerDesc(name) : MTGUIScreen.selectionDesc(name);
    }
    
    private TextAPI<?> wrapperName(ParameterLink link) {
        ParameterWrapper wrapper = link.getWrapper();
        String name = wrapper.getName();
        if(wrapper instanceof AudioRef) return TextHelper.getLiteral(name);
        else if(wrapper instanceof From) name = "from";
        else if(wrapper instanceof Link) name = "link";
        else if(wrapper instanceof Loop) name = "loop";
        else if(wrapper instanceof RecordElement) return MTGUIScreen.jukeboxName(link);
        else if(wrapper instanceof RedirectElement) return MTGUIScreen.redirectName(link);
        else if(wrapper instanceof To) name = "to";
        else if(wrapper instanceof TriggerAPI)
            return MTGUIScreen.triggerName(name,String.valueOf(link.getModifiedValue("identifier")),wrapper instanceof HolderTrigger);
        return MTGUIScreen.selectionName(name);
    }
    
    @Override public void populateToml(Toml toml) {
        Toml parent = toml;
        String typeName = this.type.getType();
        if(Misc.equalsAny(typeName,"main","renders")) parent = Toml.getEmpty();
        for(WrapperElement wrapper : this.wrappers) {
            Toml next = Toml.getEmpty();
            wrapper.link.populateToml(next);
            if(!next.getEntryValuesAsMap().isEmpty() || !next.getAllTables().isEmpty()) {
                String tableName = wrapper.link.getTypeName();
                if(tableName.equals("universal_audio")) tableName = "universal";
                parent.addTable(tableName,next);
            }
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
            if(!next.getEntryValuesAsMap().isEmpty() || !next.getAllTables().isEmpty()) {
                String tableName = wrapper.link.getTypeName();
                if(tableName.equals("universal_triggers")) tableName = "universal";
                parent.addTable(tableName,next);
            }
        }
        if(typeName.equals("main") && !parent.getAllTables().isEmpty()) toml.addTable("triggers",parent);
        else if(typeName.equals("renders") && !parent.getAllTables().isEmpty()) toml.addTable("title",parent);
    }
    
    public static final class WrapperElement {
        
        final WrapperLink parent;
        final ParameterWrapper wrapper;
        DataLink link;
        Widget widget;
        
        WrapperElement(WrapperLink parent, ParameterWrapper wrapper) {
            this.parent = parent;
            this.wrapper = wrapper;
        }
        
        void setLink(MTScreenInfo type) {
            String nextType = this.wrapper.getName();
            if(this.wrapper instanceof RedirectElement) nextType = "redirect_element";
            else if(this.wrapper instanceof RecordElement) nextType = "jukebox_element";
            else if(this.wrapper instanceof Link) nextType = "link";
            else if(this.wrapper instanceof Loop) nextType = "loop";
            else if(this.wrapper instanceof From) nextType = "from";
            else if(this.wrapper instanceof To) nextType = "to";
            DataLink link = null;
            for(DataLink next : type.getCache())
                if(next instanceof ParameterLink && ((ParameterLink)next).getWrapper()==this.wrapper)
                    link = next;
            if(Objects.isNull(link)) link = MTGUIScreen.findLink(type,nextType,this.wrapper);
            MTScreenInfo next = type.next(nextType,link);
            if(Objects.nonNull(link)) link.setType(next);
            this.link = link;
        }
        
        public @Nullable ParameterLink getAsParameter() {
            return this.link instanceof ParameterLink ? (ParameterLink)this.link : null;
        }
        
        TextAPI<?> getDisplayName() {
            return this.parent.wrapperName((ParameterLink)this.link);
        }
        
        TextAPI<?> getDescription() {
            return this.parent.wrapperDesc(((ParameterLink)this.link).getWrapper());
        }
        
        public void setDisplayName(TextAPI<?> text) {
            if(this.widget instanceof Button) {
                Button button = (Button)this.widget;
                button.setText(text);
                button.setHoverText(w -> w.setText(text));
            }
        }
    }
}
