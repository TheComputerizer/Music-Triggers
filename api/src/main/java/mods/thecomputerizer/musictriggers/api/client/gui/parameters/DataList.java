package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicWidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetList;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Shape;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;

import java.util.Objects;
import java.util.function.Consumer;

import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.AQUA;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.BLACK;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.WHITE;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;

public class DataList extends WidgetList {
    
    public DataList(double x, double y, double width, double height, double spacing) {
        super(TextWidget.literal(""),x,y,width,height);
        this.spacing = spacing;
    }
    
    public void addButton(TextAPI<?> text, Consumer<Button> onClick, TextAPI<?> ... hoverLines) {
        Button button = makeButton(text,onClick);
        for(TextAPI<?> line : hoverLines)
            if(Objects.nonNull(line)) button.addHoverLine(line);
        addWidget(button);
    }
    
    public Button makeButton(TextAPI<?> text, Consumer<Button> onClick) {
        Shape shape = ShapeHelper.plane(Y,0.75d,0.1d);
        ShapeWidget widget = ShapeWidget.from(shape,BLACK.withAlpha(0f));
        TextWidget w = TextWidget.from(text);
        Widget hover = BasicWidgetGroup.from(0.75d,0.1d,ShapeWidget.from(shape,WHITE.withAlpha(1f/3f)),w.copy().setColor(AQUA));
        Button button = new Button(widget,w,hover);
        button.setClickFunc(onClick);
        return button;
    }
    
    public SpecialButton makeSpecialButton(TextAPI<?> text, Consumer<SpecialButton> onClick) {
        Shape shape = ShapeHelper.plane(Y,0.75d,0.1d);
        ShapeWidget widget = ShapeWidget.from(shape,BLACK.withAlpha(0f));
        TextWidget w = TextWidget.from(text);
        Widget hover = BasicWidgetGroup.from(0.75d,0.1d,ShapeWidget.from(shape,WHITE.withAlpha(1f/3f)),w.copy().setColor(AQUA));
        SpecialButton button = new SpecialButton(widget,w,hover);
        button.setClickFunc(b -> onClick.accept((SpecialButton)b));
        return button;
    }
}
