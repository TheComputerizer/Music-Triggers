package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.data.log.LogMessage;
import mods.thecomputerizer.musictriggers.api.data.log.MTLogger;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetList;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer;

import java.util.Objects;

import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer.Allignment.LEFT;

public class LogVisualizer extends MTGUIScreen {
    
    public LogVisualizer(ScreenAPI parent, MinecraftWindow window, int guiScale) {
        super(parent,"log",window,guiScale);
        addTextBackground(0d,0d,2d,1.8d,v -> {
            WidgetList list = new ReverseList(TextWidget.literal("message"));
            list.setSpacing(0.05d);
            for(LogMessage message : MTLogger.getGUISnapshot()) {
                TextBuffer buffer = TextBuffer.literalBuilder(message.getDisplay())
                        .setColor(message.getColor()).setAllignment(LEFT).setTranslateX(list.getScrollBar().getWidth()*2d).build();
                double textWidth = list.getWidth()*0.45d;
                TextWidget text = TextWidget.from(buffer);
                text.setWidth(textWidth);
                list.addWidget(text);
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