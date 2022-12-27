package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialElement;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialProgressBar;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;

import javax.vecmath.Point2i;
import javax.vecmath.Point4i;

public class GuiRadial extends GuiSuperType {

    protected RadialElement circleButton;

    public GuiRadial(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
    }

    private Integer[] setCenterCircle() {
        return new Integer[]{(int)(((float)this.width)/2f),(int)(((float)this.height)/2f),50,100,25};
    }

    protected RadialProgressBar createRadialProgressBar() {
        return null;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.circleButton = type.getCircleForType(this, setCenterCircle(), createRadialProgressBar());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        circleButton.mousePressed(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawStuff(int mouseX, int mouseY, float partialTicks) {
        circleButton.render(this.zLevel,mouseX,mouseY);
        GuiUtil.drawColoredRing(new Point2i((int)(((float)this.width)/2f),(int)(((float)this.height)/2f)),
                new Point2i(35,37),new Point4i(255,255,255,192),100,this.zLevel);
        GuiUtil.drawColoredRing(new Point2i((int)(((float)this.width)/2f),(int)(((float)this.height)/2f)),
                new Point2i(110,112),new Point4i(255,255,255,192),100,this.zLevel);
    }

    @Override
    protected void save() {

    }


}
