package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicTypeableWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer;

public class TextBox extends BasicTypeableWidget {
    
    private Widget background;
    private Widget backgroundHover;
    
    public TextBox(TextBuffer text, double x, double y) {
        super(text,x,y,-1);
    }
    
    @Override public TextBox copy() {
        TextBox copy = new TextBox(this.text.copy(),this.x,this.y);
        copy.copyBasic(this);
        return copy;
    }
    
    @Override public void draw(RenderContext ctx, Vector3d center, double mouseX, double mouseY) {
        super.draw(ctx,center,mouseX,mouseY);
    }
}