package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.data.log.LogMessage;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetList;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.FuzzBall;

public class MTLogVisualizer extends MTGUI {
    
    public static void open(MinecraftWindow window) {
        ScreenHelper.open(new MTLogVisualizer(window,ClientHelper.getGuiScale()));
        MTGUI.isActive = true;
    }
    
    public MTLogVisualizer(MinecraftWindow window, int guiScale) {
        super("log",window,guiScale);
        WidgetList list = new ReverseList(TextWidget.literal("message"));
        list.setSpacing(0.05d);
        for(LogMessage message : MTLogger.getGUISnapshot()) {
            list.addWidgetFromTemplate((widget,index) -> {
                TextWidget text = (TextWidget)widget;
                text.setText(message.getDisplay()).setColor(message.getColor());
            });
        }
        addWidget(list);
    }
    
    @Override protected FuzzBall makeFuzzGenerator(int min, int max, float minWidth, float maxWidth) {
        return null;
    }
    
    static class ReverseList extends WidgetList {
        
        public ReverseList(Widget template) {
            super(template,0d,0d,2d,2d);
        }
        
        @Override protected Vector3d calculatePosition(Widget widget, int index) {
            Vector3d pos = super.calculatePosition(widget,index);
            double elementsHeight = getElementsHeight();
            double height = getHeight();
            if(elementsHeight>height) this.scrollOffset = elementsHeight-height;
            return pos;
        }
    }
}