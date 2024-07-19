package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.client.gui.ParameterScreen;
import mods.thecomputerizer.musictriggers.api.client.gui.WrapperScreen;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef.InterruptHandler;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventRunner.EventInstance;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicWidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.GenericUtils;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import static mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen.open;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.*;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer.Alignment.TOP_CENTER;

@Getter
public class ParameterLink extends DataLink {
    
    private final Set<ParameterElement> parameters;
    @Getter private final Set<DataLink> children;
    @Setter private ParameterWrapper wrapper;
    
    public ParameterLink(ParameterWrapper wrapper, Map<String,Parameter<?>> parameters) {
        super(false);
        this.parameters = new HashSet<>();
        this.children = new HashSet<>();
        this.wrapper = wrapper;
        TableRef ref = wrapper.getReferenceData();
        for(Entry<String,Parameter<?>> entry : parameters.entrySet())
            this.parameters.add(new ParameterElement(this,entry.getKey(),ref,entry.getValue()));
        if(wrapper instanceof RecordElement) {
            RecordElement record = (RecordElement)wrapper;
            this.parameters.add(new ParameterElement(this,"jukebox_key",null,new ParameterString(record.getKey())));
            this.parameters.add(new ParameterElement(this,"jukebox_value",null,new ParameterString(record.getValue())));
        }
        else if(wrapper instanceof RedirectElement) {
            RedirectElement record = (RedirectElement)wrapper;
            this.parameters.add(new ParameterElement(this,"redirect_key",null,new ParameterString(record.getKey())));
            this.parameters.add(new ParameterElement(this,"redirect_value",null,new ParameterString(record.getValue())));
        }
    }
    
    public void addChildren(MTGUIScreen screen, DataList list) {
        for(DataLink link : this.children) {
            list.addButton(link.getDisplayName(),b -> {
                link.type.setLink(link);
                MinecraftWindow window = ClientHelper.getWindow();
                int scale = ClientHelper.getGuiScale();
                open(link instanceof ParameterLink ? new ParameterScreen(screen,link.type,window,scale) :
                             new WrapperScreen(screen,link.type,window,scale));
            },link.getDescription());
        }
    }
    
    public void distributeExternalChange(String name, Object value) {
        for(ParameterElement element : this.parameters) {
            if(element.name.equals(name)) {
                element.modifiable.setValue(value);
                element.refreshWidget();
                return;
            }
        }
    }
    
    @Override public TextAPI<?> getDescription() {
        return MTGUIScreen.selectionDesc(this.type.getType());
    }
    
    @Override public TextAPI<?> getDisplayName() {
        return MTGUIScreen.selectionName(this.type.getType());
    }
    
    public @Nullable Object getModifiedValue(String name) {
        for(ParameterElement element : this.parameters)
            if(element.name.equals(name)) return element.getValue();
        return null;
    }
    
    @Override public void populateToml(Toml toml) {
        for(ParameterElement parameter : this.parameters) {
            Object value = parameter.getValue();
            if(Misc.equalsAny(this.wrapper.getReferenceData().getName(),"channel","debug") ||
               !GenericUtils.matches(parameter.ref.getDefaultValue(),value))
                toml.addEntry(parameter.name,value);
        }
        this.type.populateNext(toml,true);
    }
    
    @Override public void setType(MTScreenInfo type) {
        this.type = type;
        if(this.children.isEmpty()) this.children.addAll(this.wrapper.getChildWrappers(this.type));
    }
    
    public static final class ParameterElement {
        
        final ParameterLink parent;
        @Getter final String name;
        final ParameterRef<?> ref;
        final Parameter<?> original;
        Parameter<?> modifiable;
        Widget widget;
        
        ParameterElement(ParameterLink parent, String name, @Nullable TableRef ref, Parameter<?> parameter) {
            this.parent = parent;
            this.name = name;
            this.ref = Objects.nonNull(ref) ? ref.findParameter(name) : null;
            this.original = parameter;
            this.modifiable = parameter.copy();
        }
        
        public TextAPI<?> getDescription() {
            String name = this.name;
            if(name.equals("priority") && this.parent.wrapper instanceof InterruptHandler) name = "interrupt_"+name;
            if(this.parent.wrapper instanceof EventInstance) name = "event_"+name;
            return MTGUIScreen.parameterDesc(name);
        }
        
        public TextAPI<?> getHover() {
            return TextHelper.getLiteral(this.name);
        }
        
        public TextAPI<?> getDisplayName() {
            String name = this.name;
            if(name.equals("priority") && this.parent.wrapper instanceof InterruptHandler) name = "interrupt_"+name;
            if(this.parent.wrapper instanceof EventInstance) name = "event_"+name;
            return MTGUIScreen.parameterName(name);
        }
        
        public String getLiteralValue() {
            return String.valueOf(this.modifiable.getValue());
        }
        
        public Object getValue() {
            return this.modifiable.getValue();
        }
        
        void refreshWidget() {
            if(this.widget instanceof WidgetGroup) {
                for(Widget widget : ((WidgetGroup)this.widget).getWidgets()) {
                    if(widget instanceof CheckBox) ((CheckBox)widget).setSelected((Boolean)getValue());
                    else if(widget instanceof DataList) {
                        DataList data = (DataList)widget;
                        Collection<Widget> widgets = data.getWidgets();
                        List<?> updated = (List<?>)getValue();
                        Widget button = null;
                        for(Widget w : widgets) {
                            if(w instanceof SpecialButton) {
                                button = w;
                                break;
                            }
                        }
                        widgets.clear();
                        for(Object value : updated) {
                            TextBuffer text = TextBuffer.literal(String.valueOf(value));
                            data.addWidget(new TextBox(this,text,0d,0d,widget.getWidth()));
                        }
                        if(Objects.nonNull(button)) data.addWidget(button);
                    } else if(widget instanceof TextBox) ((TextBox)widget).setText(getLiteralValue());
                }
            }
        }
        
        public void save(Object value) {
            String previous = String.valueOf(this.modifiable.getValue());
            this.modifiable.setValue(value);
            this.parent.setModified(true);
            MTScreenInfo type = this.parent.type;
            if(this.parent.wrapper instanceof RecordElement)
                type.distributeJukeboxChange(this.parent);
            else if(this.parent.wrapper instanceof RedirectElement)
                type.distributeRedirectChange(this.parent,previous);
            else if(this.name.equals("identifier") && this.parent.wrapper instanceof TriggerAPI)
                type.distributeIdentifierChange(this.parent,getLiteralValue(),previous);
        }
        
        public Widget toWidget(ParameterScreen screen) {
            if(Objects.isNull(this.widget)) {
                WidgetGroup group = BasicWidgetGroup.from(-0.5d,0d,0.95d,2d);
                group.addWidget(TextWidget.from(getDisplayName(),0d,0.75d));
                group.addWidget(ShapeWidget.outlineFrom(0.95d,0.25d,0d,0.75d));
                DataList list = new DataList(0d,0.25d,0.95d,0.75d,0.05d);
                list.addWidget(TextWidget.from(TextBuffer.getBuilder(getDescription()).setAlignment(TOP_CENTER).build()));
                group.addWidget(list);
                group.addWidget(ShapeWidget.outlineFrom(0.95d,0.75d,0d,0.25d));
                valueModifierWidgets(screen,group);
                this.widget = group;
            }
            if(!screen.getWidgets().contains(this.widget)) screen.addWidget(this.widget);
            return this.widget;
        }
        
        private void valueModifierWidgets(ParameterScreen screen, WidgetGroup group) {
            double x = 0d;
            double y = -0.5d;
            if(this.original.isBool())
                group.addWidget(new CheckBox(this,0.25d,x,y));
            /*
            else if(this.ref.hasPotentialValues()) {
                Shape shape = ShapeHelper.plane(Y,0.95d,0.05d);
                TextWidget text = TextWidget.literal(getLiteralValue());
                Button button = new Button(ShapeWidget.from(shape,BLACK),text,ShapeWidget.from(shape,WHITE.withAlpha(1f/3f)));
                button.setX(x);
                button.setY(y);
                //button.setClickFunc(b -> this.activeDropDown = Objects.isNull(this.activeDropDown) ? potentialValues(b,this.activeParameter) : null);
                button.setParent(screen);
            }
             */
            else if(this.original.isList()) {
                DataList list = new DataList(x,y,0.95d,0.75d,0.05d);
                for(Object element : (Collection<?>)getValue()) {
                    TextBuffer text = TextBuffer.literal(String.valueOf(element));
                    list.addWidget(new TextBox(this,text,0d,0d,list.getWidth()));
                }
                SpecialButton add = list.makeSpecialButton(this.parent.type.getSpecialLang(
                        "gui","button.add_entry.name"),b -> {
                    Collection<Widget> widgets = list.getWidgets();
                    for(Widget w : widgets)
                        if(w instanceof SpecialButton && ((SpecialButton)w).isDeleting()) return;
                    widgets.remove(b);
                    TextBuffer text = TextBuffer.literal("_");
                    list.addWidget(new TextBox(this,text,0d,0d,list.getWidth()));
                    widgets.add(b);
                    list.setWidgets(widgets);
                    this.parent.setModified(true);
                });
                add.getText().setColor(GRAY.withAlpha(0.7f));
                add.setContextFunc(b -> {
                    SpecialButton specialButton = (SpecialButton)b;
                    boolean delete = specialButton.isDeleting();
                    b.getText().setColor(delete ? GRAY.withAlpha(0.7f) : RED);
                    TextAPI<?> text = this.parent.type.getSpecialLang("gui",
                            "button."+(delete ? "add" : "remove")+"_entry.name");
                    b.setText(text);
                    b.setHoverText(h -> {
                        h.setColor(delete ? AQUA : DARK_RED);
                        h.setText(text);
                    });
                    specialButton.setDeleting(!delete);
                    ScreenHelper.playVanillaClickSound();
                });
                list.addWidget(add);
                group.addWidget(list);
            }
            else group.addWidget(new TextBox(this,TextBuffer.literal(getLiteralValue()),x,y,0.95d));
            group.addWidget(ShapeWidget.outlineFrom(0.95d,0.75d,x,y));
        }
    }
}
