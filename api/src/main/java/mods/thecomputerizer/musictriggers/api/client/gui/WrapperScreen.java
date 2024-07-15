package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataList;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.SelectionLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.WrapperLink;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;

import java.util.Objects;
import java.util.function.Supplier;

public class WrapperScreen extends MTGUIScreen {
    
    public WrapperScreen(ScreenAPI parent, MTScreenInfo typeInfo, MinecraftWindow window, int guiScale) {
        super(parent,typeInfo,window,guiScale);
        addTextBackground(v -> {
            DataLink data = this.typeInfo.getLink();
            if(data instanceof WrapperLink) {
                WrapperLink link = (WrapperLink)data;
                addElements(() -> link.getList(this),() -> link.getOtherList(this));
            } else if(data instanceof SelectionLink) {
                SelectionLink link = (SelectionLink)data;
                addElements(() -> link.getList(this),() -> link.getOtherList(this));
            }
        });
    }
    
    public <D extends DataLink> void addElements(Supplier<DataList> getList, Supplier<DataList> getOtherList) {
        DataList list = getList.get();
        addWidget(list);
        double typeOffset = -list.getScrollBar().getWidth();
        DataList otherList = getOtherList.get();
        if(Objects.nonNull(otherList)) {
            addWidget(otherList);
            autoAddTypeTexture(-otherList.getScrollBar().getWidth());
            typeOffset-=1d;
        }
        autoAddTypeTexture(typeOffset);
    }
    
    public float defaultBackgroundDarkness() {
        return 0.6f;
    }
    
    public void draw(RenderContext ctx, Vector3d center, double mouseX, double mouseY) {
        if(Objects.nonNull(this.parentScreen))
            this.parentScreen.draw(ctx,center,mouseX+9999d,mouseY+9999d); //Add a large offset to prevent hover behavior
        super.draw(ctx,center,mouseX,mouseY);
    }
}