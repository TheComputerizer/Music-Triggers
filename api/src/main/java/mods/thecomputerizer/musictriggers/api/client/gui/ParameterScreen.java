package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.shadow.org.joml.Vector2d;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicWidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetList;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Shape;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;

import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.TABLE_MAP;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.*;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;

public class ParameterScreen extends MTGUIScreen {
    
    private final TableRef ref;
    private final Toml toml;
    private TomlEntry<?> activeEntry;
    private Widget activeWidget;
    
    public ParameterScreen(ScreenAPI parent, Toml toml, String type, MinecraftWindow window, int guiScale) {
        super(parent,type,window,guiScale);
        this.ref = TABLE_MAP.get(type);
        this.toml = toml;
        addTextBackground(0d,0d,2d,1.8d,v -> {
            WidgetList list = WidgetList.from(TextWidget.literal(""),0.5d,0d,1d,1.8d,0.05d);
            for(TomlEntry<?> entry : toml.getAllEntries()) {
                Button button = parameterButton(TextHelper.getLiteral(entry.getKey()));
                button.setClickFunc(b -> toWidget(entry));
                list.addWidget(button);
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
        if(Objects.nonNull(this.activeWidget)) this.activeWidget.draw(ctx,center,mouseX,mouseY);
    }
    
    private Button parameterButton(TextAPI<?> text) {
        Shape shape = ShapeHelper.plane(Y,new Vector2d(-0.375d,-0.05d),new Vector2d(0.375d,0.05d));
        ShapeWidget widget = ShapeWidget.from(shape,BLACK.withAlpha(0f));
        TextWidget w = TextWidget.from(text);
        Widget hover = BasicWidgetGroup.from(ShapeWidget.from(shape,WHITE.withAlpha(1f/3f)),w.copy().setColor(AQUA));
        return new Button(widget,w,hover);
    }
    
    private void toWidget(TomlEntry<?> entry) {
        this.activeEntry = entry;
        this.activeWidget = TextWidget.literal(entry.getKey()+" (TODO)",-0.5d,0d);
    }
}