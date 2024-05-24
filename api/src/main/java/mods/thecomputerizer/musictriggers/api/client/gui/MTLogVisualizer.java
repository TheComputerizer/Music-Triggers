package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.data.log.LogMessage;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.shadow.org.joml.Vector2d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetList;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Plane;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;

import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.BLACK;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer.Allignment.LEFT;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;

public class MTLogVisualizer extends MTGUI {
    
    public static void open(ScreenAPI parent, MinecraftWindow window) {
        ScreenHelper.open(new MTLogVisualizer(parent,window,ClientHelper.getGuiScale()));
        MTGUI.isActive = true;
    }
    
    public MTLogVisualizer(ScreenAPI parent, MinecraftWindow window, int guiScale) {
        super(parent,"log",window,guiScale);
        WidgetList list = new ReverseList(TextWidget.literal("message"));
        Plane back = ShapeHelper.plane(Y,new Vector2d(-1d,-0.9d),new Vector2d(1d,0.9d));
        addWidget(ShapeWidget.from(back,BLACK.withAlpha(0.65f)));
        list.setSpacing(0.05d);
        for(LogMessage message : MTLogger.getGUISnapshot()) {
            TextBuffer buffer = TextBuffer.literalBuilder(message.getDisplay())
                    .setColor(message.getColor()).setAllignment(LEFT).setTranslateX(0.05d).build();
            double textWidth = list.getWidth()*0.45d;
            TextWidget text = TextWidget.from(buffer);
            text.setWidth(textWidth);
            list.addWidget(text);
        }
        addWidget(list);
        addWidget(ShapeWidget.outlineFrom(back,5f));
        addBackButton();
    }
    
    static class ReverseList extends WidgetList {
        
        public ReverseList(Widget template) {
            super(template,0d,0d,2d,1.8d);
        }
        
        @Override protected void recalculatePositions() {
            super.recalculatePositions();
            double height = getHeight();
            double elementsHeight = getElementsHeight();
            if(elementsHeight>height) this.scrollOffset = elementsHeight-height;
            setScrollBarPos();
        }
    }
}