package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.client.gui.ParameterScreen;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicWidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetList;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Shape;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.BLACK;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.WHITE;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;

@Getter
public class ParameterLink extends DataLink {
    
    private final Set<ParameterElement> parameters;
    
    public ParameterLink(@Nullable MTScreenInfo type, TableRef ref, Map<String,Parameter<?>> parameters) {
        super(type,false);
        this.parameters = new HashSet<>();
        for(Entry<String,Parameter<?>> entry : parameters.entrySet())
            this.parameters.add(new ParameterElement(this,entry.getKey(),ref,entry.getValue()));
    }
    
    private TextAPI<?> parameterDesc(String name) {
        return TextHelper.getTranslated(parameterLang(name, "desc"));
    }
    
    private String parameterLang(String name, String suffix) {
        return String.format("parameter.%1$s.%2$s.%3$s",MODID,name,suffix);
    }
    
    private TextAPI<?> parameterName(String name) {
        return TextHelper.getTranslated(parameterLang(name,"name"));
    }
    
    public static final class ParameterElement {
        
        final ParameterLink parent;
        @Getter final String name;
        final ParameterRef<?> ref;
        final Parameter<?> original;
        Parameter<?> modifiable;
        Widget widget;
        
        ParameterElement(ParameterLink parent, String name, TableRef ref, Parameter<?> parameter) {
            this.parent = parent;
            this.name = name;
            this.ref = ref.findParameter(name);
            this.original = parameter;
            this.modifiable = parameter.copy();
        }
        
        public TextAPI<?> getDescription() {
            return this.parent.parameterDesc(this.name);
        }
        
        public TextAPI<?> getDisplayName() {
            return this.parent.parameterName(this.name);
        }
        
        public String getLiteralValue() {
            return String.valueOf(this.modifiable.getValue());
        }
        
        public Object getValue() {
            return this.modifiable.getValue();
        }
        
        public void save(Object value) {
            this.modifiable.setValue(value);
        }
        
        public Widget toWidget(ParameterScreen screen) {
            if(Objects.isNull(this.widget)) {
                WidgetGroup group = BasicWidgetGroup.from(-0.5d, 0d, 0.95d, 2d);
                group.addWidget(TextWidget.from(getDisplayName(), 0d, 0.5d));
                group.addWidget(TextWidget.from(getDescription(), 0d, 0.25d));
                valueModifierWidgets(screen,group);
                this.widget = group;
                screen.addWidget(group);
            }
            return this.widget;
        }
        
        private void valueModifierWidgets(ParameterScreen screen, WidgetGroup group) {
            double x = 0d;
            double y = -0.5d;
            if(this.original.isBool())
                group.addWidget(new CheckBox(this,0.25d,x,y));
            else if(this.ref.hasPotentialValues()) {
                Shape shape = ShapeHelper.plane(Y,0.95d,0.05d);
                TextWidget text = TextWidget.literal(getLiteralValue());
                Button button = new Button(ShapeWidget.from(shape,BLACK),text,ShapeWidget.from(shape,WHITE.withAlpha(1f/3f)));
                button.setX(x);
                button.setY(y);
                //button.setClickFunc(b -> this.activeDropDown = Objects.isNull(this.activeDropDown) ? potentialValues(b,this.activeParameter) : null);
                button.setParent(screen);
            } else if(this.original.isString())
                group.addWidget(new TextBox(this,TextBuffer.literal(getLiteralValue()),x,y,0.95d));
            else if(this.original.isList()) {
                WidgetList list = WidgetList.from(TextWidget.literal(""),x,y,0.95d,0.75d,0.05d);
                for(Object element : (Collection<?>)getValue()) {
                    TextBuffer text = TextBuffer.literal(String.valueOf(element));
                    list.addWidget(new TextBox(this,text,x,y,list.getWidth()));
                }
                group.addWidget(list);
                group.addWidget(ShapeWidget.outlineFrom(0.95d,0.75d,x,y));
            }
        }
    }
}
