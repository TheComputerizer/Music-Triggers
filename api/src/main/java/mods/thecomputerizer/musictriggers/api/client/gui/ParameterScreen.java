package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicWidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetList;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Shape;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.TABLE_MAP;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.*;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;

public class ParameterScreen extends MTGUIScreen {
    
    private final TableRef ref;
    private final Toml toml;
    private TomlEntry<?> activeEntry;
    private ParameterRef<?> activeParameter;
    private Widget activeWidget;
    private Widget activeDropDown;
    
    public ParameterScreen(ScreenAPI parent, Toml toml, String type, MinecraftWindow window, int guiScale) {
        super(parent,type,window,guiScale);
        this.ref = TABLE_MAP.get(type);
        this.toml = toml;
        addTextBackground(0d,0d,2d,1.8d,v -> {
            WidgetList list = WidgetList.from(TextWidget.literal(""),0.5d,0d,1d,1.8d,0.05d);
            if(Objects.nonNull(this.toml)) {
                for(TomlEntry<?> entry : toml.getAllEntries()) {
                    Button button = parameterButton(parameterName(entry.getKey()));
                    button.setClickFunc(b -> toWidget(entry));
                    list.addWidget(button);
                }
            }
            addWidget(list);
            addTypeTexture(-list.getScrollBar().getWidth(),0d);
        });
    }
    
    public float defaultBackgroundDarkness() {
        return 0.6f;
    }
    
    public void draw(RenderContext ctx, Vector3d center, double mouseX, double mouseY) {
        if(Objects.nonNull(this.parentScreen))
            this.parentScreen.draw(ctx,center,mouseX+9999d,mouseY+9999d); //Add a large offset to prevent hover behavior
        super.draw(ctx,center,mouseX,mouseY);
    }
    
    private Button parameterButton(TextAPI<?> text) {
        Shape shape = ShapeHelper.plane(Y,0.75d,0.1d);
        ShapeWidget widget = ShapeWidget.from(shape,BLACK.withAlpha(0f));
        TextWidget w = TextWidget.from(text);
        Widget hover = BasicWidgetGroup.from(ShapeWidget.from(shape,WHITE.withAlpha(1f/3f)),w.copy().setColor(AQUA));
        return new Button(widget,w,hover);
    }
    
    private TextAPI<?> parameterDesc(String name) {
        return TextHelper.getTranslated(parameterLang(name,"desc"));
    }
    
    private String parameterLang(String name, String suffix) {
        return String.format("parameter.%1$s.%2$s.%3$s",MODID,name,suffix);
    }
    
    private TextAPI<?> parameterName(String name) {
        return TextHelper.getTranslated(parameterLang(name,"name"));
    }
    
    private WidgetList potentialValues(Button template, ParameterRef<?> ref) {
        List<Widget> potentials = new ArrayList<>();
        for(Object potentialValue : ref.getPotentialValues()) {
            Button potential = template.copy();
            potential.setText(String.valueOf(potentialValue));
            potential.setClickFunc(b -> saveActiveEntryAs(potentialValue));
            potentials.add(potential);
        }
        return WidgetHelper.dropDownFrom(template,-0.9d,potentials);
    }
    
    public void saveActiveEntryAs(Object value) {
        if(Objects.nonNull(this.activeEntry)) this.activeEntry = this.toml.addEntry(this.activeEntry.getKey(),value);
    }
    
    private void toWidget(TomlEntry<?> entry) {
        this.activeEntry = entry;
        if(Objects.nonNull(this.ref)) {
            for(ParameterRef<?> parameter : this.ref.getParameters()) {
                if(entry.getKey().equals(parameter.getName())) {
                    this.activeParameter = parameter;
                    break;
                }
            }
        }
        WidgetGroup group = BasicWidgetGroup.from(-0.5d,0d,0.95d,2d);
        group.addWidget(TextWidget.from(parameterName(entry.getKey()),0d,0.5d));
        group.addWidget(TextWidget.from(parameterDesc(entry.getKey()),0d,0.25d));
        valueModifierWidgets(group,entry);
        if(Objects.nonNull(this.activeWidget)) this.widgets.remove(this.activeWidget);
        this.activeWidget = group;
        addWidget(this.activeWidget);
    }
    
    private void valueModifierWidgets(WidgetGroup group, TomlEntry<?> entry) {
        Object value = entry.getValue();
        double x = 0d;
        double y = -0.5d;
        if(value instanceof Boolean)
            group.addWidget(new CheckBox((Boolean)value,0.25d,x,y));
        else if(Objects.nonNull(this.activeParameter) && this.activeParameter.hasPotentialValues()) {
            Shape shape = ShapeHelper.plane(Y,0.95d,0.05d);
            TextWidget text = TextWidget.literal(String.valueOf(value));
            Button button = new Button(ShapeWidget.from(shape,BLACK),text,ShapeWidget.from(shape,WHITE.withAlpha(1f/3f)));
            button.setX(x);
            button.setY(y);
            button.setClickFunc(b -> this.activeDropDown = Objects.isNull(this.activeDropDown) ?
                    potentialValues(b,this.activeParameter) : null);
            button.setParent(this);
        } else if(value instanceof String)
            group.addWidget(new TextBox(TextBuffer.literal((String)value),x,y,0.95d));
        else if(value instanceof Collection<?>) {
            WidgetList list = WidgetList.from(TextWidget.literal(""),x,y,0.95d,0.75d,0.05d);
            for(Object element : (Collection<?>)value) {
                TextBuffer text = TextBuffer.literal(String.valueOf(element));
                list.addWidget(new TextBox(text,x,y,list.getWidth()));
            }
            group.addWidget(list);
            group.addWidget(ShapeWidget.outlineFrom(0.95d,0.75d,x,y));
        }
    }
    
    public enum ParameterConstraints {
        CHARACTER_BLACKLIST,
        DECIMAL,
        INTEGER,
        NUMBER_RANGE,
        RELOAD_REQUIRED,
        RESTART_REQUIRED
    }
    
    public enum ParameterType {
        BOOLEAN,
        DROPDOWN,
        LIST,
        NUMBER,
        STRING
    }
}