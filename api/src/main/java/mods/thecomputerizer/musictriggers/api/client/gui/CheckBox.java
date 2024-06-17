package mods.thecomputerizer.musictriggers.api.client.gui;

import lombok.Setter;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Selectable;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextureWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.Shape;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.ShapeHelper;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.ColorHelper.WHITE;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.block.Facing.Axis.Y;

public class CheckBox extends WidgetGroup implements Selectable {
    
    public static final TextureWrapper CHECKED = checkboxTexture(true,false);
    public static final TextureWrapper CHECKED_HOVER = checkboxTexture(true,true);
    public static final TextureWrapper UNCHECKED = checkboxTexture(false,false);
    public static final TextureWrapper UNCHECKED_HOVER = checkboxTexture(false,true);
    
    private static TextureWrapper checkboxTexture(boolean checked, boolean hover) {
        String path = String.format("textures/gui/%1$sicon/%2$schecked.png",hover ? "hover/" : "",checked ? "" : "un");
        return TextureWrapper.of(MODID,path);
    }
    
    private final ShapeWidget checked;
    private final ShapeWidget checkedHover;
    private final ShapeWidget hoverBackground;
    private final ShapeWidget unchecked;
    private final ShapeWidget uncheckedHover;
    @Setter private boolean selected;
    private final double sideLength;
    
    public CheckBox(boolean selected, double sideLength, double x, double y) {
        this.selected = selected;
        this.sideLength = sideLength;
        Shape shape = ShapeHelper.square(Y,sideLength,RenderHelper.getCurrentHeightRatio());
        this.checked = ShapeWidget.from(shape,CHECKED);
        this.checkedHover = ShapeWidget.from(shape,CHECKED_HOVER);
        this.hoverBackground = ShapeWidget.from(shape.getScaled(1.05d),WHITE.withAlpha(1f/3f));
        this.unchecked = ShapeWidget.from(shape,UNCHECKED);
        this.uncheckedHover = ShapeWidget.from(shape,UNCHECKED_HOVER);
        addWidgets(this.checked,this.checkedHover,this.hoverBackground,this.unchecked,this.uncheckedHover);
        setX(x);
        setY(y);
    }
    
    @Override public CheckBox copy() {
        CheckBox copy = new CheckBox(this.selected,this.sideLength,this.x,this.y);
        copy.copyGroup(this);
        return copy;
    }
    
    @Override public void draw(RenderContext ctx, Vector3d center, double mouseX, double mouseY) {
        if(canDraw()) {
            if(this.selected) {
                if(isHovering(mouseX,mouseY)) {
                    this.hoverBackground.draw(ctx,center,mouseX,mouseY);
                    this.checkedHover.draw(ctx,center,mouseX,mouseY);
                }
                else this.checked.draw(ctx,center,mouseX,mouseY);
            } else {
                if(isHovering(mouseX,mouseY)) {
                    this.hoverBackground.draw(ctx,center,mouseX,mouseY);
                    this.uncheckedHover.draw(ctx,center,mouseX,mouseY);
                }
                else this.unchecked.draw(ctx,center,mouseX,mouseY);
            }
        }
    }
    
    @Override public boolean isHovering(double x, double y) {
        return this.checked.isInside(x,y,0d);
    }
    
    @Override public boolean isSelected() {
        return this.selected;
    }
    
    @Override public void drawSelected(RenderContext ctx, Vector3d center, double mouseX, double mouseY) {
        draw(ctx,center,mouseX,mouseY);
    }
    
    @Override public boolean onLeftClick(double mouseX, double mouseY) {
        if(isHovering(mouseX,mouseY)) {
            this.selected = !this.selected;
            playLeftClickSound();
            return true;
        }
        return false;
    }
    
    @Override public boolean onRightClick(double mouseX, double mouseY) {
        return false;
    }
    
    @Override public void playLeftClickSound() {
        ScreenHelper.playVanillaClickSound();
    }
    
    @Override public void playRightClickSound() {}
}