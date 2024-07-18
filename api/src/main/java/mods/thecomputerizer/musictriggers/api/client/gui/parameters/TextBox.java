package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink.ParameterElement;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicTypeableWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Shape;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.AQUA;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.RED;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.WHITE;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;

public class TextBox extends BasicTypeableWidget {
    
    private final ParameterElement link;
    private final Widget backgroundHover;
    private final Shape backgroundShape;
    
    public TextBox(ParameterElement link, TextBuffer text, double x, double y, double width) {
        super(text,x,y,-1);
        this.link = link;
        this.backgroundShape = ShapeHelper.plane(Y,width,RenderHelper.getScaledFontHeight()*1.5d);
        this.backgroundHover = ShapeWidget.from(this.backgroundShape,WHITE.withAlpha(1f/3f));
        this.backgroundHover.setParent(this);
    }
    
    @Override public TextBox copy() {
        TextBox copy = new TextBox(link,this.text.copy(),this.x,this.y,this.width);
        copy.copyBasic(this);
        this.cursorBlinkCounter = copy.cursorBlinkCounter;
        this.selected = copy.selected;
        return copy;
    }
    
    @Override public void draw(RenderContext ctx, Vector3d center, double mouseX, double mouseY) {
        if(this.backgroundShape.isInside(mouseX-getX(),mouseY-getY()-center.y,0d)) {
            this.backgroundHover.draw(ctx,center,mouseX,mouseY);
            this.colorOverride = AQUA;
        } else this.colorOverride = WHITE;
        super.draw(ctx,center,mouseX,mouseY);
    }
    
    @Override public boolean onLeftClick(double x, double y) {
        if(this.backgroundShape.isInside(x-getX(),y-getY(),0d)) {
            boolean remove = false;
            if(this.parent instanceof DataList) {
                DataList list = (DataList)this.parent;
                Collection<Widget> widgets = ((DataList)this.parent).getWidgets();
                for(Widget widget : widgets) {
                    if(widget instanceof SpecialButton && ((SpecialButton)widget).isDeleting()) {
                        remove = true;
                        break;
                    }
                }
                if(remove) {
                    widgets.remove(this);
                    list.setWidgets(widgets);
                    this.link.parent.setModified(true);
                    return true;
                }
            }
            this.selected = true;
            double width = getWidth();
            double parentWidth = Objects.nonNull(this.parent) ? this.parent.getWidth() : 0d;
            double height = getHeight();
            Vector3d center = getCenter(0d);
            int pos = this.text.getCharPos(RenderHelper.getContext(),x,y,getCenter(0d),
                                           getMinX(center.x,width,parentWidth),getMinY(center.y,height),getMaxX(center.x,width,parentWidth),
                                           getMaxY(center.y,height));
            if(pos!=-1) {
                this.text.setBlinkerPos(pos);
                return true;
            }
        } else {
            this.text.setBlinkerVisible(false);
            this.selected = false;
        }
        return false;
    }
    
    @Override protected void onTextAdded(String text) {
        super.onTextAdded(text);
        trySaving();
    }
    
    @Override protected String onTextRemoved() {
        String removed = super.onTextRemoved();
        if(StringUtils.isNotEmpty(removed)) trySaving();
        return removed;
    }
    
    private void trySaving() {
        if(this.parent instanceof DataList) {
            List<String> text = new ArrayList<>();
            for(Widget widget : ((DataList)this.parent).getWidgets())
                if(widget instanceof TextBox) text.add(widget.toString());
            this.link.save(text);
        } else this.link.save(toString());
    }
}